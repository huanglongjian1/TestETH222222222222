package com.example.mylibrary_eth

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mylibrary_eth.databinding.EthActivityBinding
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService

class ETHActivity : AppCompatActivity() {
    private val mBinding by lazy {
        EthActivityBinding.inflate(layoutInflater)
    }

    //  private val web3jUrl = "https://sepolia.infura.io/v3/b032a1fc8f554d7a99f0d7bdab9a8295"

    private val ethUtil by lazy {
        val web3jUrl = "http://10.0.2.2:8545"
        ETHUtil(Web3j.build(HttpService(web3jUrl)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        mBinding.createETHWallet.setOnClickListener {
            ethUtil.createWallet(getExternalFilesDir("ETHUtil"))
        }
        getExternalFilesDir("ETHUtil")?.listFiles()?.let {
            if (it.isNotEmpty()) {
                mBinding.addressEt.setText(ethUtil.loadWallet(it[0]).address)
                mBinding.getBalance.setOnClickListener { _ ->
                    Loge.e(mBinding.addressEt.text.toString().trim())
                    val balanceString =
                        ethUtil.getBalance(mBinding.addressEt.text.toString().trim())
                    Loge.e(balanceString)
                }
            } else {
                mBinding.addressEt.setText("还没有创建钱包")
            }
        }
        mBinding.transaction.setOnClickListener {
            ethUtil.loadWallet(getExternalFilesDir("ETHUtil")!!.listFiles()?.get(0))
                .let {
                    lifecycleScope.launch(Dispatchers.IO) {
                        ethUtil.transaction("0xef02ea79b5f95d147580cdd14aa742506bc2a6ab", it) {
                            lifecycleScope.launch(Dispatchers.Main) {
                                mBinding.info.text =
                                    mBinding.info.text.toString() + "\n" + it.to + ":" + it.gas
                            }
                        }
                    }
                }
        }



    }
}

fun <E> SendChannel<E>.safeSend(value: E) = try {
    trySend(value)
} catch (e: CancellationException) {
    e.printStackTrace()
}

fun View.clickFlow(): Flow<View> {
    return callbackFlow<View> {
        setOnClickListener {
            safeSend(it)
        }
        awaitClose { setOnClickListener(null) }
    }
}