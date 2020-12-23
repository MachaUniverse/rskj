/*
 * This file is part of RskJ
 * Copyright (C) 2017 RSK Labs Ltd.
 * (derived from ethereumJ library, Copyright (c) 2016 <ether.camp>)
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

package org.ethereum.vm.program;

import co.rsk.core.Coin;
import co.rsk.core.RskAddress;
import co.rsk.core.bc.AccountInformationProvider;
import co.rsk.crypto.Keccak256;
import co.rsk.trie.Trie;
import com.google.common.annotations.VisibleForTesting;
import org.ethereum.core.AccountState;
import org.ethereum.core.Repository;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.program.invoke.ProgramInvoke;
import org.ethereum.vm.program.listener.ProgramListener;
import org.ethereum.vm.program.listener.ProgramListenerAware;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.Set;

/*
 * A Storage is a proxy class for Repository. It encapsulates a repository providing tracing services.
 * It is only used by Program.
 * It does not provide any other functionality different from tracing.
 */
public class Storage implements Repository, ProgramListenerAware {

    private final Repository repository;
    private final RskAddress addr;
    private ProgramListener traceListener;

    public Storage(ProgramInvoke programInvoke) {
        this.addr = new RskAddress(programInvoke.getOwnerAddress());
        this.repository = programInvoke.getRepository();
    }

    @Override
    public void setTraceListener(ProgramListener listener) {
        this.traceListener = listener;
    }

    @Override
    public Trie getTrie() {
        return repository.getTrie();
    }

    @Override
    public AccountState createAccount(RskAddress addr) {
        return repository.createAccount(addr);
    }

    @Override
    public void setupContract(RskAddress addr) {
        repository.setupContract(addr);
    }

    @Override
    public boolean isExist(RskAddress addr, boolean trackRent) {
        return repository.isExist(addr, trackRent);
    }

    @Override
    public boolean isExist(RskAddress addr) {
        return repository.isExist(addr);
    }

    @Override
    public AccountState getAccountState(RskAddress addr, boolean trackRent) {
        return repository.getAccountState(addr,trackRent );
    }

    @Override
    public AccountState getAccountState(RskAddress addr) {
        return repository.getAccountState(addr);
    }

    @Override
    @VisibleForTesting
    public long getAccountNodeLRPTime(RskAddress addr) {
        return repository.getAccountNodeLRPTime(addr);
    }

    @Override
    public void delete(RskAddress addr) {
        if (canListenTrace(addr)) {
            traceListener.onStorageClear();
        }
        repository.delete(addr);
    }

    @Override
    public void hibernate(RskAddress addr) {
        repository.hibernate(addr);
    }

    @Override
    public BigInteger increaseNonce(RskAddress addr) {
        return repository.increaseNonce(addr);
    }

    @Override
    public void setNonce(RskAddress addr, BigInteger nonce) {
        repository.setNonce(addr, nonce);
    }

    @Override
    public BigInteger getNonce(RskAddress addr,boolean trackRent) {
        return repository.getNonce(addr,trackRent);
    }

    @Override
    public void saveCode(RskAddress addr, byte[] code) {
        repository.saveCode(addr, code);
    }

    @Override
    public byte[] getCode(RskAddress addr, boolean trackRent) {
        return repository.getCode(addr,trackRent);
    }

    @Override
    public int getCodeLength(RskAddress addr, boolean trackRent) {
        return repository.getCodeLength(addr, trackRent);
    }

    @Override
    public int getCodeLength(RskAddress addr) {
        return repository.getCodeLength(addr);
    }

    @Override
    public Keccak256 getCodeHashNonStandard(RskAddress addr, boolean trackRent) {
        return repository.getCodeHashNonStandard(addr, trackRent);
    }

    @Override
    public Keccak256 getCodeHashNonStandard(RskAddress addr) {
        return repository.getCodeHashNonStandard(addr);
    }

    @Override
    public Keccak256 getCodeHashStandard(RskAddress addr, boolean trackRent) {
        return repository.getCodeHashStandard(addr,trackRent );
    }

    @Override
    public Keccak256 getCodeHashStandard(RskAddress addr) {
        return repository.getCodeHashNonStandard(addr);
    }


    @Override
    public boolean isContract(RskAddress addr,boolean trackRent) {
        return repository.isContract(addr,trackRent);
    }

    @Override
    public void addStorageRow(RskAddress addr, DataWord key, DataWord value) {
        if (canListenTrace(addr)) {
            traceListener.onStoragePut(key, value);
        }
        repository.addStorageRow(addr, key, value);
    }

    @Override
    public void addStorageBytes(RskAddress addr, DataWord key, byte[] value) {
        if (canListenTrace(addr)) {
            traceListener.onStoragePut(key, value);
        }
        repository.addStorageBytes(addr, key, value);
    }

    private boolean canListenTrace(RskAddress addr) {
        return this.addr.equals(addr) && traceListener != null;
    }

    @Override
    public DataWord getStorageValue(RskAddress addr, DataWord key,boolean trackRent) {
        return repository.getStorageValue(addr, key,trackRent);
    }

    @Override
    public Coin getBalance(RskAddress addr) {
        return repository.getBalance(addr);
    }

    @Nullable
    @Override
    public DataWord getStorageValue(RskAddress addr, DataWord key) {
        return repository.getStorageValue(addr,key);
    }

    @Nullable
    @Override
    public byte[] getStorageBytes(RskAddress addr, DataWord key) {
        return repository.getStorageBytes(addr,key);
    }

    @Override
    public Iterator<DataWord> getStorageKeys(RskAddress addr) {
        return repository.getStorageKeys(addr);
    }

    @Override
    public int getStorageKeysCount(RskAddress addr) {
        return repository.getStorageKeysCount(addr);
    }

    @Nullable
    @Override
    public byte[] getCode(RskAddress addr) {
        return repository.getCode(addr);
    }

    @Override
    public boolean isContract(RskAddress addr) {
        return repository.isContract(addr);
    }

    @Override
    public BigInteger getNonce(RskAddress addr) {
        return repository.getNonce(addr);
    }

    @Override
    public byte[] getStorageBytes(RskAddress addr, DataWord key,boolean trackRent) {
        return repository.getStorageBytes(addr, key,trackRent);
    }

    @Override
    public Coin getBalance(RskAddress addr,boolean trackRent) {
        return repository.getBalance(addr,trackRent);
    }

    @Override
    public Coin addBalance(RskAddress addr, Coin value) {
        return repository.addBalance(addr, value);
    }

    @Override
    public Set<RskAddress> getAccountsKeys() {
        return repository.getAccountsKeys();
    }

    @Override
    public Repository startTracking() {
        return repository.startTracking();
    }

    @Override
    public void commit() {
        repository.commit();
    }

    @Override
    public void save() {
        repository.save();
    }

    @Override
    public void rollback() {
        repository.rollback();
    }

    @Override
    public byte[] getRoot() {
        return repository.getRoot();
    }

    @Override
    public void updateAccountState(RskAddress addr, AccountState accountState) {
        throw new UnsupportedOperationException();
    }
}
