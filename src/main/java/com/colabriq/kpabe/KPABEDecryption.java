package com.colabriq.kpabe;

import static com.colabriq.kpabe.KPABEUtil.DECRYPTION_FAILED;
import static com.colabriq.kpabe.KPABEUtil.checkResult;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.util.concurrent.locks.Lock;

import com.colabriq.kpabe.jna.KPABELibrary;
import com.colabriq.kpabe.jna.KPABELibrary.COutString;
import com.colabriq.kpabe.jna.KPABELibraryLoader;
import com.colabriq.kpabe.jna.KPABELibraryLock;
import com.colabriq.kpabe.key.KPABEPublicKey;
import com.colabriq.kpabe.key.KPABEShareKey;

/**
 * Decryption-related things you can do with KPABE.
 * @author ijmad
 */
public class KPABEDecryption {
	private static final Lock lock = KPABELibraryLock.LOCK;
	
	public static KPABEDecryption getInstance() {
		return new KPABEDecryption(KPABELibraryLoader.getInstance());
	}
	
	private final KPABELibrary library;	
	
	KPABEDecryption(KPABELibrary library) {
		this.library = library;
	}
	
	public String decrypt(String ciphertext, KeyPair keys) throws KPABEException, InvalidKeyException {
		lock.lock();
		
		try {
			Key publicKey = keys.getPublic();
			Key shareKey = keys.getPrivate();
			
			if ((publicKey instanceof KPABEPublicKey) && (shareKey instanceof KPABEShareKey)) {
				COutString.ByReference clearText = new COutString.ByReference();
	
				int result = library.decrypt(publicKey.toString(), ciphertext, shareKey.toString(), clearText);
				if (result == DECRYPTION_FAILED) {
					return null;
				}
				
				checkResult(result);
				return clearText.toString();
			}
			else {
				throw new InvalidKeyException("Must be ABE keys");
			}
		}
		finally {
			lock.unlock();
		}
	}
}
