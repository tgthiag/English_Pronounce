package com.tgapps.englishpronounce.functions

import android.content.Context
import android.telephony.TelephonyManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.*

class Translate(var ctx: Context) {
    var tm = ctx.getSystemService(AppCompatActivity.TELEPHONY_SERVICE) as TelephonyManager
    var countryCodeValue = tm.networkCountryIso
    fun getLanguage(country: String) : String{
        var current : String
        when{
            country.contains("br") && Locale.getDefault().language.equals("en") ->  current = Locale.forLanguageTag("pt").toString()
            country.contains("in") && Locale.getDefault().language.equals("en") ->  current = Locale.forLanguageTag("hi").toString()
            country.contains("pk") && Locale.getDefault().language.equals("en") ->  current = Locale.forLanguageTag("ur").toString()
            else -> current = Locale.getDefault().language
        }
//        Toast.makeText(ctx,current,Toast.LENGTH_LONG).show()
        return current
    }
    var lang = getLanguage(countryCodeValue)
    var tradOpt = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.ENGLISH)
        .setTargetLanguage(TranslateLanguage.fromLanguageTag(lang).toString())
        .build()
    val traduzir_pergunta = Translation.getClient(tradOpt)
    var conditions = DownloadConditions.Builder()
        .build()

    fun download() {
        traduzir_pergunta.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
            }
            .addOnFailureListener { exception ->
                // Model couldn’t be downloaded or other internal error.
            }
    }
    fun question(cxW: TextView, cxT: TextView) {
        traduzir_pergunta.translate(cxW.text.toString())
            .addOnSuccessListener {
                cxT.text = it
            }
            .addOnFailureListener {}
    }
    fun tradBts (btSpeak : Button, btListen : TextView, txtPronThat: TextView){
        traduzir_pergunta.translate(btSpeak.text.toString()).addOnSuccessListener { btSpeak.text = it }
        traduzir_pergunta.translate(btListen.text.toString()).addOnSuccessListener { btListen.text = it }
        traduzir_pergunta.translate(txtPronThat.text.toString()).addOnSuccessListener { txtPronThat.text = it }
    }
}