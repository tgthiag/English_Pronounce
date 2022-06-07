package com.tgapps.englishpronounce.view

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.GridView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.tgapps.englishpronounce.Word_Item
import com.tgapps.englishpronounce.data.FRASE
import com.tgapps.englishpronounce.data.LocalDatabase
import com.tgapps.englishpronounce.data.Phrases
import com.tgapps.englishpronounce.data.TABLE_NAME
import com.tgapps.englishpronounce.databinding.ActivityMainBinding
import com.tgapps.englishpronounce.functions.Translate
import com.tgapps.englishpronounce.functions.VoiceRecognition
import com.tgapps.englishpronounce.recyclerview.WordViewAdapter
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


private lateinit var mTTS: TextToSpeech
private lateinit var binding: ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var palavra: Int = 0
    private var gridView: GridView? = null
    private var arrayList: ArrayList<Word_Item>? = null
    private var wordAdapter: WordViewAdapter? = null

    lateinit var lstValues: List<String>
    var rawvalues: String = ""
    private var voiceResult: String = ""
    var faladas = mutableListOf<String>()

    //    val REQ_CODE_SPEECH_INPUT = 100
    //    var dbPhrase : Int = 0
    lateinit var db: SQLiteDatabase
    var query = "SELECT * FROM pronounce_app"


    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.hide()

        //BAIXANDO TRADUÇÕES
        Translate(this).download()

        //INICIANDO TTS
        mTTS = TextToSpeech(this, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR) {
                mTTS.setLanguage(Locale.US)
                mTTS.speak(binding.txComplete.text.toString(), TextToSpeech.QUEUE_FLUSH, null, null)
            }
        })
        Translate(this).tradBts(binding.dks, binding.btListen, binding.txSpeakThat)

        //INICIANDO BANCO DE DADOS
        LocalDatabase(this).initializeRow()
        db = LocalDatabase(this).writableDatabase
        //DEFININDO FRASE ATUAL
        var cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        var sqlFrase = cursor.getString(2).toInt()
        updateList(Phrases(this).phrases[sqlFrase])

        //DEFININDO GRIDVIEW E ADAPTER E ATUALIZANDO COM NOVA FRASE
        gridView = binding.gridViewww
        wordAdapter = WordViewAdapter(
            applicationContext,
            arrayList,
            faladas,
            binding.txComplete,
            binding.txCompleteTransl
        )
        gridView!!.adapter = wordAdapter

        //FALAR AO CLICAR NO BOX DE FRASE INTEIRA
        binding.completeBox.setOnClickListener {
            mTTS = TextToSpeech(this, TextToSpeech.OnInitListener { status ->
                if (status != TextToSpeech.ERROR) {
                    mTTS.setLanguage(Locale.US)
                    mTTS.speak(
                        binding.txComplete.text.toString(),
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        null
                    )
                }
            })
        }

        //COLOCAR FRASE COMPLETA NO BOX
        binding.txPronunciar.text = lstValues[palavra]

        //BOTÃO DE FALAR
        binding.dks.setOnClickListener {
            //speechInput()
            VoiceRecognition(this, binding.speechResult).listen()
//            runForever(false)
//                lifecycleScope.launch {
//                    delay(200)
//                    checkPalavra(binding.speechResult.text.toString().lowercase())
//                }

        }

        binding.speechResult.doAfterTextChanged { checkPalavra(binding.speechResult.text.toString().lowercase()) }

        //BOTÃO DE OUVIR
        binding.btListen.setOnClickListener {
            mTTS = TextToSpeech(this, TextToSpeech.OnInitListener { status ->
                if (status != TextToSpeech.ERROR) {
                    mTTS.setLanguage(Locale.US)
                    mTTS.speak(
                        binding.txPronunciar.text.toString(),
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        null
                    )
                }
            })
        }
        //mTTS.setOnUtteranceProgressListener(object : )
    }

    override fun onPause() {
        super.onPause()
        mTTS.stop()
    }

    private fun updateList(string: String) {
        //Toast.makeText(this, "iniciado", Toast.LENGTH_SHORT).show()
        rawvalues = string//"Someone that never made mistakes never did something new."
        var values = rawvalues.replace("!", "").replace(".", "").replace("?", "").replace(",", "").lowercase()
        var splited = values.split(",", " ", "!", "?", ".", ", ").map { it -> it.trim() }
        lstValues = splited.filter { it.length > 2 }
            .filter { it != "did" && it != "the" && it != "that" &&
                    it != "those" && it != "this" && it != "are" &&
                    it != "turn" && it != "than" && it != "has" &&
                    it != "would" && it != "can"}.distinct()
        arrayList = ArrayList()
        lstValues.forEach { it ->
            arrayList!!.add(Word_Item(it, ""))
            binding.txComplete.text = rawvalues
//            print("$it\n")
        }
        wordAdapter?.notifyDataSetChanged()
        gridView?.invalidateViews()
        gridView?.adapter = WordViewAdapter(this, arrayList, faladas, binding.txComplete, binding.txCompleteTransl)
    }


    fun checkPalavra(str: String) {
        var pronunciada = binding.txPronunciar.text.toString().lowercase().trim().replace("!", "")
            .replace(".", "").replace("?", "").replace(",", "")
        var flip: Boolean = str.lowercase().trim().contains(pronunciada)
        if (flip) {
            lifecycleScope.launch {
                binding.imageCheck.visibility = View.VISIBLE
                delay(1500)
                binding.imageCheck.visibility = View.INVISIBLE
            }
            faladas.add(pronunciada)
            wordAdapter?.notifyDataSetChanged()
            gridView?.invalidateViews()
            gridView?.adapter = WordViewAdapter(
                this,
                arrayList,
                faladas,
                binding.txComplete,
                binding.txCompleteTransl
            )
            if (palavra < lstValues.size - 1) {
//            binding.txPronunciadas.text = binding.txPronunciadas.text.toString() + " " + binding.txPronunciar.text.toString()
                palavra += 1
                binding.txPronunciar.text = lstValues[palavra]
                Log.e("teste", "menor $palavra  $flip")
            } else if (palavra == lstValues.size - 1) {
//            binding.txPronunciadas.text = binding.txPronunciadas.text.toString() + " " + binding.txPronunciar.text.toString()
                palavra += 1
                binding.txPronunciar.text = rawvalues
                Log.e("teste", "igual $palavra  $flip")
            } else if (palavra > lstValues.size - 1) {
                Log.e("teste", " maior $palavra  $flip")
                var cursor = db.rawQuery(query, null)
                cursor.moveToFirst()
                var sqlFrase = cursor.getString(2).toInt() + 1
                var cv = ContentValues()
                cv.put(FRASE, sqlFrase)
                db.update(TABLE_NAME, cv, null, null)
                updateList(Phrases(this).phrases[sqlFrase])
                palavra = 0
                faladas.clear()
//            binding.txPronunciadas.text = ""
                binding.txPronunciar.text = lstValues[palavra]
            }
        }else{
            lifecycleScope.launch {
                binding.imageXis.visibility = View.VISIBLE
                delay(1500)
                binding.imageXis.visibility = View.INVISIBLE
                this.coroutineContext.cancel()
            }
        }

    }

}