package com.example.dompetku.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.example.dompetku.LoginActivity
import com.example.dompetku.R
import com.example.dompetku.SessionManager
import com.example.dompetku.adapter.AdapterRiwayat
import com.example.dompetku.dataclass.DataRiwayat
import com.facebook.shimmer.ShimmerFrameLayout
import org.json.JSONArray
import org.json.JSONObject


class RiwayatFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var dataRiwayat : ArrayList<DataRiwayat>
    private lateinit var sessionManager: SessionManager
    private lateinit var shimmer: ShimmerFrameLayout
    private lateinit var swipe: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_riwayat, container, false)
        recyclerView = view.findViewById(R.id.recyclerRiwayat)
        dataRiwayat = ArrayList<DataRiwayat>()
        sessionManager = SessionManager(activity)
        shimmer = view.findViewById(R.id.shimmer)
        swipe = view.findViewById(R.id.swipe)

        getTransactions()
        swipe.setOnRefreshListener {
            dataRiwayat.clear()
            getTransactions()
            swipe.isRefreshing = false
        }
        return view
    }

    private fun getTransactions() {
        startShimmer()
        val token = sessionManager.getToken()

        AndroidNetworking.get("https://dompetku-api.vercel.app/api/transaction")
            .addHeaders("Authorization", "Bearer $token")
            .setTag("profile")
            .setPriority(Priority.LOW)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.d("response", response.toString())
                    val data: JSONArray = response.getJSONArray("data")
                    if(response.getString("success").equals("true")) {
                        for (i in 0 until data.length()) {
                            val item = data.getJSONObject(i)
                            dataRiwayat.add(
                                DataRiwayat(
                                    item.getString("_id"),
                                    item.getString("type"),
                                    item.getInt("amount"),
                                    item.getString("createdAt"),
                                    item.getString("status"),
                                    item.getString("icon")
                                )
                            )

                            recyclerView.layoutManager = LinearLayoutManager(activity)
                            recyclerView.adapter = activity?.let { AdapterRiwayat(it, dataRiwayat) }
                        }

                        stopShimmer()
                    }
                }

                override fun onError(error: ANError) {
                    val error = error.errorBody
                    val jsonObject = JSONObject(error)

                    if(jsonObject.getString("code").equals("401")) {
                        val intent = Intent(activity, LoginActivity::class.java)
                        startActivity(intent)
                    }
                }
            })
    }

    private fun startShimmer() {
        shimmer.visibility = View.VISIBLE
        shimmer.startShimmer()
    }

    private fun stopShimmer() {
        shimmer.visibility = View.GONE
        shimmer.stopShimmer()
    }
}