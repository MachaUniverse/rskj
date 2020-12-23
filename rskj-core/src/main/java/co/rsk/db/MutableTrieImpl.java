/*
 * This file is part of RskJ
 * Copyright (C) 2019 RSK Labs Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package co.rsk.db;

import co.rsk.core.RskAddress;
import co.rsk.core.types.ints.Uint24;
import co.rsk.crypto.Keccak256;
import co.rsk.trie.*;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.TrieKeyMapper;
import org.ethereum.vm.DataWord;

import javax.annotation.Nullable;
import java.util.*;

public class MutableTrieImpl implements MutableTrie {

    private Trie trie;
    private TrieKeyMapper trieKeyMapper = new TrieKeyMapper();
    private TrieStore trieStore;

    public MutableTrieImpl(TrieStore trieStore, Trie trie) {
        this.trieStore = trieStore;
        this.trie = trie;
    }

    @Override
    public Trie getTrie() {
        return trie;
    }


    @Override
    public Keccak256 getHash() {
        return trie.getHash();
    }

    @Override
    public TrieNodeData getNodeData(byte[] key) {
        Trie node = trie.find(key);
        if (node == null) {
            return null;
        }
        return (TrieNodeData) node;
        /*
        TrieNodeData nodeData = new TrieNodeData(
                value,
                node.getChildrenSize().value,node.getLastRentPaidTime(),
                node.getValueLength().intValue()
        );
        return nodeData;
        */


    }

    @Override
    public byte[] get(byte[] key) {
        return trie.get(key);
    }

    @Override
    public void put(byte[] key, byte[] value) {
        trie = trie.put(key, value);
    }

    @Override
    public void put(ByteArrayWrapper key, byte[] value) {
        trie = trie.put(key, value);
    }

    @Override
    public void put(String key, byte[] value) {
        trie = trie.put(key, value);
    }

    @Override
    public long getValueLength(byte[] key) {
        Trie atrie = trie.find(key);
        if (atrie == null) {
            return 0;
        }

        return atrie.getValueLength();
    }

    @Override
    public Uint24 getValueLengthForOptionalUse(byte[] key) {
        return getValueLengthForEncoding(key);
    }

    //@Override
    public Uint24 getValueLengthForEncoding(byte[] key) {
        Trie atrie = trie.find(key);
        if (atrie == null) {
            // TODO(mc) should be null?
            return Uint24.ZERO;
        }

        return atrie.getValueLengthForEncoding();
    }

    @Override
    public Optional<Keccak256> getValueHash(byte[] key) {
        Trie atrie = trie.find(key);
        if (atrie == null) {
            return Optional.empty();
        }
        return Optional.of(atrie.getValueHash());
    }

    @Override
    public Iterator<DataWord> getStorageKeys(RskAddress addr) {
        byte[] accountStorageKey = trieKeyMapper.getAccountStoragePrefixKey(addr);
        final int storageKeyOffset = (TrieKeyMapper.storagePrefix().length + TrieKeyMapper.SECURE_KEY_SIZE) * Byte.SIZE - 1;
        Trie storageTrie = trie.find(accountStorageKey);

        if (storageTrie != null) {
            Iterator<Trie.IterationElement> storageIterator = storageTrie.getPreOrderIterator();
            storageIterator.next(); // skip storage root
            return new StorageKeysIterator(storageIterator, storageKeyOffset);
        }
        return Collections.emptyIterator();
    }

    @Override
    public void deleteRecursive(byte[] key) {
        trie = trie.deleteRecursive(key);
    }

    @Override
    public void save() {
        if (trieStore != null) {
            trieStore.save(trie);
        }
    }

    @Override
    public void commit() {
        // TODO(mc) is it OK to leave this empty? why?
    }

    @Override
    public void rollback() {
        // TODO(mc) is it OK to leave this empty? why?
    }

    @Override
    public Set<ByteArrayWrapper> collectKeys(int size) {
        return trie.collectKeys(size);
    }

    private static class StorageKeysIterator implements Iterator<DataWord> {
        private final Iterator<Trie.IterationElement> storageIterator;
        private final int storageKeyOffset;
        private DataWord currentStorageKey;

        StorageKeysIterator(Iterator<Trie.IterationElement> storageIterator, int storageKeyOffset) {
            this.storageIterator = storageIterator;
            this.storageKeyOffset = storageKeyOffset;
        }

        @Override
        public boolean hasNext() {
            if (currentStorageKey != null) {
                return true;
            }
            while (storageIterator.hasNext()) {
                Trie.IterationElement iterationElement = storageIterator.next();
                if (iterationElement.getNode().getValue() != null) {
                    TrieKeySlice nodeKey = iterationElement.getNodeKey();
                    byte[] storageExpandedKeySuffix = nodeKey.slice(storageKeyOffset, nodeKey.length()).encode();
                    currentStorageKey = DataWord.valueOf(storageExpandedKeySuffix);
                    return true;
                }
            }
            return false;
        }

        @Override
        public DataWord next() {
            if (currentStorageKey == null && !hasNext()) {
                throw new NoSuchElementException();
            }

            DataWord next = currentStorageKey;
            currentStorageKey = null;
            return next;
        }
    }
}
