package com.example.dompetku

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.example.dompetku.adapter.AdapterMetode
import com.example.dompetku.dataclass.DataMetode
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONArray
import org.json.JSONObject

class MetodeActivity : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager
    private lateinit var dataMetode: ArrayList<DataMetode>
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_metode)

        sessionManager = SessionManager(this)
        dataMetode = ArrayList<DataMetode>()
        recyclerView = findViewById(R.id.recyclerMetode)

        btnBack = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        getMetode(this)
    }

    private fun getMetode(context: Context) {
        val token = sessionManager.getToken()

        AndroidNetworking.get("https://dompetku-api.vercel.app/api/payment")
            .addHeaders("Authorization", "Bearer $token")
            .setTag("metode")
            .setPriority(Priority.LOW)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.d("response", response.toString())
                    val data: JSONArray = response.getJSONArray("data")

                    if(response.getString("success").equals("true")) {
                        for (i in 0 until data.length()) {
                            val item = data.getJSONObject(i)
                            dataMetode.add(
                                DataMetode(
                                    item.getString("code"),
                                    item.getString("name"),
                                    item.getString("icon_url")
                                )
                            )

                            recyclerView.layoutManager = LinearLayoutManager(context)
                            recyclerView.adapter = AdapterMetode(context, dataMetode)
                        }
                    }
                }

                override fun onError(error: ANError) {
                    val error = error.errorBody
                    val jsonObject = JSONObject(error)

                    MaterialAlertDialogBuilder(this@MetodeActivity)
                        .setTitle("Gagal")
                        .setMessage(jsonObject.getString("message"))
                        .setPositiveButton("OK") { dialog, which ->
                            dialog.dismiss()
                        }
                        .show()

                    if(jsonObject.getString("code").equals("401")) {
                        val intent = Intent(this@MetodeActivity, LoginActivity::class.java)
                        startActivity(intent)
                    }
                }
            })
    }
}