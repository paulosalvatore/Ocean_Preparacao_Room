package com.oceanbrasil.ocean_preparacao_room

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val NEW_WORD_ACTIVITY_REQUEST_CODE = 1
    }

    class WordViewModelFactory(val application: Application): ViewModelProvider.AndroidViewModelFactory(application) {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T = WordViewModel(application) as T

    }

    private val wordViewModel: WordViewModel by lazy {
        ViewModelProvider(
            this,
            WordViewModelFactory(application)
        ).get(WordViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        println(wordViewModel)

        val adapter = WordListAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        wordViewModel.allWords.observe(this, Observer {
            adapter.words = it
        })

        button.setOnClickListener {
            val intent = Intent(
                this,
                NewWordActivity::class.java
            )

            startActivityForResult(
                intent,
                NEW_WORD_ACTIVITY_REQUEST_CODE
            )
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == NEW_WORD_ACTIVITY_REQUEST_CODE &&
            resultCode == Activity.RESULT_OK
        ) {
            data?.let {
                val extraReply = it.getStringExtra(NewWordActivity.EXTRA_REPLY)

                extraReply?.let { reply ->
                    val word = Word(reply)
                    wordViewModel.insert(word)
                }
            }
        } else {
            Toast.makeText(this, "Word not saved because it is empty.", Toast.LENGTH_LONG).show()
        }
    }
}
