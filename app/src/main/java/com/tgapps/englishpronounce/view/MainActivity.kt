package com.tgapps.englishpronounce.view

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.GridView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tgapps.englishpronounce.R
import com.tgapps.englishpronounce.Word_Item
import com.tgapps.englishpronounce.data.FRASE
import com.tgapps.englishpronounce.data.LocalDatabase
import com.tgapps.englishpronounce.data.Phrases
import com.tgapps.englishpronounce.data.TABLE_NAME
import com.tgapps.englishpronounce.databinding.ActivityMainBinding
import com.tgapps.englishpronounce.functions.Translate
import com.tgapps.englishpronounce.recyclerview.WordViewAdapter
import github.com.vikramezhil.dks.speech.Dks
import github.com.vikramezhil.dks.speech.DksListener
import java.util.*


private lateinit var mTTS : TextToSpeech
private lateinit var binding: ActivityMainBinding
class MainActivity : AppCompatActivity(){
    private var palavra : Int = 0
    private var gridView:GridView ? = null
    private var arrayList : ArrayList<Word_Item> ? = null
    private var  wordAdapter: WordViewAdapter? = null
    val REQ_CODE_SPEECH_INPUT = 100
    lateinit var lstValues: List<String>
    var rawvalues : String = ""
    private lateinit var dks: Dks
    private var voiceResult : String = ""
    var faladas = mutableListOf<String>()
    var dbPhrase : Int = 0
    lateinit var db :SQLiteDatabase
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
            if (status != TextToSpeech.ERROR){
                mTTS.setLanguage(Locale.US)
                mTTS.speak(binding.txComplete.text.toString(), TextToSpeech.QUEUE_FLUSH,null,null)
            }
        })
        Translate(this).tradBts(binding.dks, binding.btListen, binding.txSpeakThat)

        //INICIANDO BANCO DE DADOS
        LocalDatabase(this).initializeRow()
        db = LocalDatabase(this).writableDatabase
        //DEFININDO FRASE ATUAL
        var cursor = db.rawQuery(query,null)
        cursor.moveToFirst()
        var sqlFrase = cursor.getString(2).toInt()
        updateList(Phrases(this).phrases[sqlFrase])

        //DEFININDO GRIDVIEW E ADAPTER E ATUALIZANDO COM NOVA FRASE
        gridView = binding.gridViewww
        wordAdapter = WordViewAdapter(applicationContext,arrayList,faladas, binding.txComplete, binding.txCompleteTransl)
        gridView!!.adapter = wordAdapter

        //FALAR AO CLICAR NO BOX DE FRASE INTEIRA
        binding.completeBox.setOnClickListener{
            mTTS = TextToSpeech(this, TextToSpeech.OnInitListener { status ->
                if (status != TextToSpeech.ERROR){
                    mTTS.setLanguage(Locale.US)
                    mTTS.speak(binding.txComplete.text.toString(), TextToSpeech.QUEUE_FLUSH,null,null)
                }
            })
        }

        binding.txPronunciar.text = lstValues[palavra]


        //BOTÃO DE FALAR
        binding.dks .setOnClickListener {
            speechInput()
        }
        //BOTÃO DE OUVIR
        binding.btListen.setOnClickListener {
            mTTS = TextToSpeech(this, TextToSpeech.OnInitListener { status ->
                if (status != TextToSpeech.ERROR){
                    mTTS.setLanguage(Locale.US)
                    mTTS.speak(binding.txPronunciar.text.toString(), TextToSpeech.QUEUE_FLUSH,null,null)
                }
            })
        }

        dks = Dks(application, supportFragmentManager, object: DksListener {
            override fun onDksLiveSpeechResult(liveSpeechResult: String) {
                Log.d("DKS", "Speech result - $liveSpeechResult")
//                checkPalavra(liveSpeechResult)
            }

            override fun onDksFinalSpeechResult(speechResult: String) {
                Log.d("DKS", "Final speech result - $speechResult")
                checkPalavra(speechResult)
            }

            override fun onDksLiveSpeechFrequency(frequency: Float) {

            }

            override fun onDksLanguagesAvailable(defaultLanguage: String?, supportedLanguages: ArrayList<String>?) {
                Log.d("DKS", "defaultLanguage - $defaultLanguage")
                Log.d("DKS", "supportedLanguages - $supportedLanguages")

                if (supportedLanguages != null && supportedLanguages.contains("en-US")) {
                    // Setting the speech recognition language to english india if found
                    dks.currentSpeechLanguage = "en-US"
                }
            }

            override fun onDksSpeechError(errMsg: String) {
                Log.d("DKS", "errMsg - $errMsg")
            }
        })
        dks.currentSpeechLanguage = "en-US"
//        dks.startSpeechRecognition()
        dks.continuousSpeechRecognition = false

    }

    override fun onPause() {
        super.onPause()
        mTTS.stop()
    }

    private fun updateList(string: String) {
        rawvalues = string//"Someone that never made mistakes never did something new."
        var values = rawvalues.replace("!","").replace(".","").replace("?","").replace(",","").lowercase()
        var splited = values.split(","," ","!","?",".",", ").map { it -> it.trim() }
        lstValues = splited.filter { it.length > 2}.filter {it != "did" && it != "the" && it != "that" && it != "those" && it != "this" && it != "are" }.distinct()
        arrayList = ArrayList()
        lstValues.forEach { it ->
            arrayList!!.add(Word_Item(it,""))
            binding.txComplete.text = rawvalues
//            print("$it\n")
        }
        wordAdapter?.notifyDataSetChanged()
        gridView?.invalidateViews()
        gridView?.adapter = WordViewAdapter(this,arrayList,faladas, binding.txComplete, binding.txCompleteTransl)
    }


    /*
    * Google Speech Input prompt (Voice)
    * */

    fun speechInput() {
        dks.closeSpeechOperations()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, Locale.US.toString());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toString());
        intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, Locale.US.toString());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, binding.txPronunciar.text)


        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT)
        } catch (a: ActivityNotFoundException) {
            Toast.makeText(applicationContext,
                getString(R.string.not_supported),
                Toast.LENGTH_SHORT).show()
        }


    }
    /*
       * Displaying the dialog input (Words)
       * */
    /*
    * Displaying the dialog input (Words)
    * */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {

            REQ_CODE_SPEECH_INPUT -> if (resultCode == Activity.RESULT_OK && null != data) {
                val result: ArrayList<String> = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>
                binding.speechResult.text = result.get(0)
                checkPalavra(result.get(0))
                dks.startSpeechRecognition()
            }

        }
    }

    private fun checkPalavra(str: String) {
        var pronunciada = binding.txPronunciar.text.toString().lowercase().trim().replace("!","").replace(".","").replace("?","").replace(",","")
        var flip : Boolean = str.lowercase().trim().contains(pronunciada)
        if (flip){
            faladas.add(pronunciada)
            wordAdapter?.notifyDataSetChanged()
            gridView?.invalidateViews()
            gridView?.adapter = WordViewAdapter(this,arrayList,faladas, binding.txComplete, binding.txCompleteTransl)
            if (palavra < lstValues.size -1){
//            binding.txPronunciadas.text = binding.txPronunciadas.text.toString() + " " + binding.txPronunciar.text.toString()
                palavra += 1
                binding.txPronunciar.text = lstValues[palavra]
                Log.e("teste", "menor $palavra  $flip")
            }else if (palavra == lstValues.size -1){
//            binding.txPronunciadas.text = binding.txPronunciadas.text.toString() + " " + binding.txPronunciar.text.toString()
                palavra += 1
                binding.txPronunciar.text = rawvalues
                Log.e("teste", "igual $palavra  $flip")
            }else if (palavra > lstValues.size -1){
                Log.e("teste", " maior $palavra  $flip")
                var cursor = db.rawQuery(query,null)
                cursor.moveToFirst()
                var sqlFrase = cursor.getString(2).toInt() + 1
                var cv = ContentValues()
                cv.put(FRASE,sqlFrase)
                db.update(TABLE_NAME,cv,null,null)
                updateList(Phrases(this).phrases[sqlFrase])
                palavra = 0
                faladas.clear()
//            binding.txPronunciadas.text = ""
                binding.txPronunciar.text = lstValues[palavra]
            }
        }
    }

}