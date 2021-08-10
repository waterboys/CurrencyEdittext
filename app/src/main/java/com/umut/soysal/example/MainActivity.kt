package com.umut.soysal.example

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ///set locale
        currencyEn.locale = Locale.US

        btnShowRawValue.setOnClickListener {
            val rawValue = currencyTr.rawValue
            Toast.makeText(this, rawValue.toString(), Toast.LENGTH_SHORT).show()
            Log.e("RawValue", rawValue.toString())
            Log.e("Format Raw Value", currencyTr.formatCurrency(rawValue))
        }

    }
}
