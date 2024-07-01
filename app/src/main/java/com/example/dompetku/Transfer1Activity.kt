package com.example.dompetku

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.example.dompetku.adapter.AdapterRecentUser
import com.example.dompetku.dataclass.DataRecentUser
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONArray
import org.json.JSONObject

class Transfer1Activity : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager
    private lateinit var dataRecentUser: ArrayList<DataRecentUser>
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnBack: ImageView
    private lateinit var btnLanjut: Button
    private lateinit var editHp: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer1)

        sessionManager = SessionManager(this)
        dataRecentUser = ArrayList<DataRecentUser>()
        recyclerView = findViewById(R.id.recyclerRecent)
        editHp = findViewById(R.id.editHp)

        getRecents(this)

        btnLanjut = findViewById(R.id.btnLanjut)
        btnLanjut.setOnClickListener {
            if (editHp.query.toString().isNotEmpty()) {
                checkHp(editHp.query.toString(), this)
            }
        }

        btnBack = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun getRecents(context: Context) {
        val token = sessionManager.getToken()

        AndroidNetworking.get("https://dompetku-api.vercel.app/api/user/recents")
            .addHeaders("Authorization", "Bearer $token")
            .setTag("recents")
            .setPriority(Priority.LOW)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.d("response recent", response.toString())
                    val data: JSONArray = response.getJSONArray("data")

                    if(response.getString("success").equals("true")) {
                        for (i in 0 until data.length()) {
                            val item = data.getJSONObject(i)
                            dataRecentUser.add(
                                DataRecentUser(
                                    item.getString("_id"),
                                    item.getString("name"),
                                    item.getString("email"),
                                    item.getString("nohp"),
                                    item.getString("image")
                                )
                            )

                            recyclerView.layoutManager = GridLayoutManager(context, 4)
                            recyclerView.adapter = AdapterRecentUser(context, dataRecentUser)
                        }
                    }
                }

                override fun onError(error: ANError) {
                    val error = error.errorBody
                    val jsonObject = JSONObject(error)

                    MaterialAlertDialogBuilder(this@Transfer1Activity)
                        .setTitle("Gagal")
                        .setMessage(jsonObject.getString("message"))
                        .setPositiveButton("OK") { dialog, which ->
                            dialog.dismiss()
                        }
                        .show()

                    if(jsonObject.getString("code").equals("401")) {
                        val intent = Intent(this@Transfer1Activity, LoginActivity::class.java)
                        startActivity(intent)
                    }

                }
            })
    }

    private fun checkHp(noHp: String, context: Context) {
        val token = sessionManager.getToken()

        AndroidNetworking.get("https://dompetku-api.vercel.app/api/user/phone/$noHp")
            .addHeaders("Authorization", "Bearer $token")
            .setTag("recents")
            .setPriority(Priority.LOW)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.d("response check", response.toString())
                    val data: JSONArray = response.getJSONArray("data")

                    if(response.getString("code").equals("200")) {
                        val intent = Intent(context, Transfer2Activity::class.java)
                        intent.putExtra("nohp", noHp)
                        startActivity(intent)
                    }
                }

                override fun onError(error: ANError) {
                    val error = error.errorBody
                    val jsonObject = JSONObject(error)

                    MaterialAlertDialogBuilder(this@Transfer1Activity)
                        .setTitle("Gagal")
                        .setMessage(jsonObject.getString("message"))
                        .setPositiveButton("OK") { dialog, which ->
                            dialog.dismiss()
                        }
                        .show()

                    if(jsonObject.getString("code").equals("401")) {
                        val intent = Intent(this@Transfer1Activity, LoginActivity::class.java)
                        startActivity(intent)
                    }
                }
            })
    }
}