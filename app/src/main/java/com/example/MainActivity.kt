package com.example

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.DarkBg
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Scaffold(
          modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
        ) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(DarkBg)
              .safeDrawingPadding()
          ) {
            EdoFilterWebView(modifier = Modifier.fillMaxSize())
          }
        }
      }
    }
  }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun EdoFilterWebView(modifier: Modifier = Modifier) {
  var webViewRef: WebView? = remember { null }

  BackHandler {
    if (webViewRef?.canGoBack() == true) {
      webViewRef?.goBack()
    } else {
      webViewRef?.evaluateJavascript("if (typeof game !== 'undefined' && game.currentScreen !== 'menu') { game.goHome(); } else { null; }", null)
    }
  }

  AndroidView(
    modifier = modifier.testTag("edo_filter_webview"),
    factory = { context ->
      WebView(context).apply {
        webViewRef = this
        setBackgroundColor(0xFF101722.toInt())
        settings.apply {
          javaScriptEnabled = true
          domStorageEnabled = true
          databaseEnabled = true
          allowFileAccess = true
          allowContentAccess = true
          cacheMode = WebSettings.LOAD_DEFAULT
          useWideViewPort = true
          loadWithOverviewMode = true
          setSupportZoom(false)
        }
        webViewClient = WebViewClient()
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
        addJavascriptInterface(AndroidPrintBridge(context, this), "AndroidBridge")
        loadUrl("file:///android_asset/index.html")
      }
    },
    update = { webView ->
      webViewRef = webView
    }
  )
}

class AndroidPrintBridge(private val context: Context, private val webView: WebView) {
  @JavascriptInterface
  fun printDocument(title: String) {
    (context as? Activity)?.runOnUiThread {
      val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
      val printAdapter = webView.createPrintDocumentAdapter(title)
      printManager?.print(title, printAdapter, PrintAttributes.Builder().build())
    }
  }

  @JavascriptInterface
  fun savePdf(base64Data: String, fileName: String) {
    (context as? Activity)?.runOnUiThread {
      try {
        val bytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
        val dir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
        val file = java.io.File(dir, fileName)
        java.io.FileOutputStream(file).use { it.write(bytes) }
        android.widget.Toast.makeText(context, "PDF Stewart & Guidorizzi salvo: ${file.name}", android.widget.Toast.LENGTH_LONG).show()
      } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Salvando via impressora/PDF do sistema...", android.widget.Toast.LENGTH_SHORT).show()
        printDocument(fileName)
      }
    }
  }
}

