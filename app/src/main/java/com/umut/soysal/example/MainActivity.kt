package com.umut.soysal.example

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        testButton.setOnClickListener {
            testEdit.setSelectionSafe(5)
        }

        ///set locale
        currencyEn.locale = Locale.US

        btnShowRawValue.setOnClickListener {
            Log.e("RawValue", currencyTr.rawValue.toString())
            Log.e("getValue", currencyTr.value.toString())
            currencyTr.value = 12.345
            Log.e("setValue", "12.345")
            Log.e("RawValue", currencyTr.rawValue.toString())
            Log.e("getValue", currencyTr.value.toString())
        }

    }
}
