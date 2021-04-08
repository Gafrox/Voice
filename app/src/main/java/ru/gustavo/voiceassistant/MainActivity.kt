package ru.gustavo.voiceassistant

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import android.widget.Toast
import com.wolfram.alpha.WAEngine
import com.wolfram.alpha.WAPlainText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.StringBuilder


class MainActivity : AppCompatActivity() {

    lateinit var requestInput: TextView

    lateinit var searchesAdapter: SimpleAdapter

    val searches = mutableListOf<HashMap<String, String>>()

    lateinit var waEngine: WAEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        initViews()
        initWolframEngine()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> {
                val question = requestInput.text.toString()
                askWolfram(question)
                return true
            }
            R.id.action_voice -> {
                // Добавить ассоциацию
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun initViews() {
        requestInput = findViewById<TextView>(R.id.request_input)

        val searchesList = findViewById<ListView>(R.id.searches_list)
        searchesAdapter = SimpleAdapter(
            applicationContext,
            searches,
            R.layout.item_search,
            arrayOf("Request", "Response"),
            intArrayOf(R.id.request, R.id.response)
        )
        searchesList.adapter = searchesAdapter
    }

    fun initWolframEngine() {
        waEngine = WAEngine()
        waEngine.appID = "5XAUXY-AHEX64Q37J"
        waEngine.addFormat("plaintext")
    }

    fun askWolfram(request: String) {
        Toast.makeText(applicationContext, "Дай подумать...", Toast.LENGTH_SHORT).show()
        CoroutineScope(Dispatchers.IO).launch {

            val query = waEngine.createQuery().apply { input = request }

            val queryResult = waEngine.performQuery(query)

            val response = if (queryResult.isError) {
                queryResult.errorMessage
            } else if (!queryResult.isSuccess) {
                "Извини, я не понимаю, можешь перефразировать?"
            } else {
                val str = StringBuilder()
                for (pod in queryResult.pods) {
                    if (!pod.isError) {
                        for (subpod in pod.subpods) {
                            for (element in subpod.contents) {
                                if (element is WAPlainText) {
                                    str.append(element.text)
                                }
                            }
                        }
                    }
                }
                str.toString()
            }
            withContext(Dispatchers.Main) {
                searches.add(0, HashMap<String, String>().apply {
                    put("Request", request)
                    put("Response", response)
                })
                searchesAdapter.notifyDataSetChanged()
            }
        }
    }
}

