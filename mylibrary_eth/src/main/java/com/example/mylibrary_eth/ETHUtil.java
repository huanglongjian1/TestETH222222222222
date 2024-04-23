package com.example.mylibrary_eth;


import android.provider.UserDictionary;
import android.util.Log;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.MnemonicUtils;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.DbGetHex;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.concurrent.ExecutionException;

import io.reactivex.functions.Consumer;

public class ETHUtil {
    private Web3j web3j;

    public ETHUtil(Web3j web3j) {
        this.web3j = web3j;
    }

    public void createWallet(File dir) {
        if (!dir.exists()) dir.mkdirs();
        try {
            WalletUtils.generateNewWalletFile(
                    "198400",
                    dir
            );
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

    }

    public Credentials loadWallet(File walletFile) {
        try {
            return WalletUtils.loadCredentials("198400", walletFile);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CipherException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getBalance(String address) {
        try {
            BigInteger latest = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST)
                    .sendAsync().get().getBalance();
            BigDecimal bigDecimal = Convert.fromWei(latest.toString(), Convert.Unit.ETHER);
            String balanceString =
                    bigDecimal.setScale(8, RoundingMode.FLOOR).toPlainString() + " eth";
            return balanceString;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;

    }

    public void transaction(String toAddress, Credentials credentials, OnTransactionListener onTransactionListener) {
        Loge.e("支付方:" + credentials.getAddress() + "------收款方:" + toAddress);
        Loge.e("credentials:" + credentials.getAddress());

        web3j.transactionFlowable().subscribe(new Consumer<Transaction>() {
            @Override
            public void accept(Transaction transaction) throws Exception {
                Loge.e(transaction.getTo() + ":" + transaction.getGas());
                if (onTransactionListener != null)
                    onTransactionListener.onAcceptTransaction(transaction);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                throwable.printStackTrace();
            }
        });

        web3j.blockFlowable(false).subscribe(new Consumer<EthBlock>() {
            @Override
            public void accept(EthBlock ethBlock) throws Exception {

            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                throwable.printStackTrace();
            }
        });


        EthGetTransactionCount ethGetTransactionCount = null;
        try {
            ethGetTransactionCount = web3j.ethGetTransactionCount(
                    credentials.getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        BigInteger nonce = ethGetTransactionCount.getTransactionCount();

        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                nonce, Convert.toWei("18", Convert.Unit.GWEI).toBigInteger(),
                Convert.toWei("45000", Convert.Unit.WEI).toBigInteger(), toAddress, new BigInteger("3000000000000000000"));
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, (byte) 1, credentials);
        //     TransactionEncoder.signMessage(rawTransaction, credentials);    //这行代码会出现 only replay-protected (EIP-155) transactions allowed over RPC
        String hexValue = Numeric.toHexString(signedMessage);

        EthSendTransaction ethSendTransaction = null;
        try {
            ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if (ethSendTransaction.hasError()) {
            Log.e("+++transfer error:", ethSendTransaction.getError().getMessage());
        } else {
            String transactionHash = ethSendTransaction.getTransactionHash();
            Log.e("+++transactionHash:", "" + transactionHash);
        }



    }

    public void createMnemonic() {
        byte[] entropy = generateRandomBytes(16);//其实这个你就可以保存起来，因为是生成助记词很重要的一个熵，没办法逆推算，写入到keyStore种
        String mnemonic = MnemonicUtils.generateMnemonic(entropy);
        Loge.e(mnemonic);

        Credentials credentials = WalletUtils.loadBip39Credentials("198400", mnemonic);
        ECKeyPair ecKeyPair = credentials.getEcKeyPair();
        Loge.e(credentials.getAddress() + ":" + ecKeyPair.getPrivateKey() + ":" + ecKeyPair.getPublicKey());

    }

    /**
     * 创建一个指定长度的 byte[]
     *
     * @param length 长度
     * @return byte[]
     */
    private byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }

    public interface OnTransactionListener {
        public void onAcceptTransaction(Transaction transaction);
    }

}
