package com.tgapps.englishpronounce.recyclerview

import android.content.Context
import android.speech.tts.TextToSpeech
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.tgapps.englishpronounce.R
import com.tgapps.englishpronounce.Word_Item
import com.tgapps.englishpronounce.functions.Translate
import java.util.*

private lateinit var mTTS : TextToSpeech
class WordViewAdapter(var ctx: Context, var array: ArrayList<Word_Item>?, var faladas: MutableList<String>, var txComplete : TextView, var txCompleteTransl : TextView) : BaseAdapter() {
    override fun getCount(): Int {
        return array!!.size
    }

    override fun getItem(position: Int): Any {
        return array!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view : View = View.inflate(ctx, R.layout.grid_item_list,null)
        var word:TextView = view.findViewById(R.id.tx_word)
        var translation:TextView = view.findViewById(R.id.tx_transl)
        var wordItem : Word_Item = array!![position]
        word.text = wordItem.word
        translation.text = wordItem.transl
        Translate(ctx).question(word,translation)
        Translate(ctx).question(txComplete, txCompleteTransl)
        view.setOnClickListener{
            mTTS = TextToSpeech(ctx, TextToSpeech.OnInitListener { status ->
                if (status != TextToSpeech.ERROR){
                    mTTS.setLanguage(Locale.US)
                    mTTS.setSpeechRate(0.7F)
                    mTTS.setPitch(0.7F)
                    mTTS.speak(word.text.toString(),TextToSpeech.QUEUE_FLUSH,null,null)
                }
            })
        }
        for (i in 0 until faladas.size){
            if (faladas[i].lowercase() == word.text.toString().lowercase()){
                view.setBackgroundResource(R.drawable.container_green)
            }
        }
        return view
    }
}