package core.extensions

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import java.util.concurrent.atomic.AtomicLong




fun View.genId(minValue: Int = 0): Int {
    var generatedId: Int
    do {
        generatedId = View.generateViewId()
    } while (generatedId <= minValue)
    this.id = generatedId
    return generatedId
}
/**
 * Esconde o teclado virtual se ele estiver sendo exibido para a view atual.
 * Esta função é útil para esconder o teclado ao tocar fora de um campo de edição de texto,
 * por exemplo.
 *
 * Uso:
 * myEditText.hideKeyboard()
 * ```
 *
 * @return True se a requisição para esconder o teclado foi bem sucedida, False caso contrário.
 */
fun View.hideKeyboard(): Boolean {
    try {
        // Obtém o serviço InputMethodManager do sistema.
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        // Solicita ao InputMethodManager para esconder o teclado.
        return inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    } catch (ignored: RuntimeException) {
        // Captura qualquer exceção RuntimeException, que pode ocorrer se o teclado não estiver visível.
        // Retorna false em caso de erro.
    }
    return false
}

/**
 * Exibe o teclado virtual para a view atual.
 * Esta função é útil para mostrar o teclado automaticamente ao abrir um campo de edição de texto,
 * por exemplo.
 *
 * Uso:
 * myEditText.showKeyboard()
 * ```
 */
fun View.showKeyboard() {
    // Obtém o serviço InputMethodManager do sistema.
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    // Define o foco na view atual.
    this.requestFocus()
    // Solicita ao InputMethodManager para mostrar o teclado.
    imm.showSoftInput(this, 0)
}

/**
 * Torna a view visível. Equivalente a definir a visibilidade como View.VISIBLE.
 *
 * Uso:
 * myView.show()
 * ```
 */
fun View.show(){
    this.visibility = View.VISIBLE
}

/**
 * Torna a view invisível, mas ainda ocupa espaço no layout.
 * Equivalente a definir a visibilidade como View.INVISIBLE.
 *
 * Uso:
 * myView.hide()
 * ```
 */
fun View.hide() {
    this.visibility = View.INVISIBLE
}

/**
 * Torna a view invisível e não ocupa espaço no layout.
 * Equivalente a definir a visibilidade como View.GONE.
 *
 * Uso:
 * myView.gone()
 * ```
 */
fun View.gone(){
    this.visibility = View.GONE
}

/**
 * Aplica uma animação de fade out (desaparecimento gradual) à view.
 *
 * Uso:
 * // Fade out com duração padrão (500ms) e visibilidade INVISIBLE após a animação.
 * myView.fadeOutAnimation()
 *
 * // Fade out com duração de 1 segundo e visibilidade GONE após a animação.
 * myView.fadeOutAnimation(duration = 1000, visibility = View.GONE)
 *
 * // Fade out com callback executado após a animação.
 * myView.fadeOutAnimation {
 *     // Faça algo após a animação.
 * }
 * ```
 *
 * @param duration Duração da animação em milissegundos. Padrão: 500ms.
 * @param visibility Visibilidade da view após a animação.
 *                   Pode ser View.VISIBLE, View.INVISIBLE ou View.GONE.
 *                   Padrão: View.INVISIBLE.
 * @param completion Função a ser executada após o término da animação (opcional).
 */
fun View.fadeOutAnimation(duration: Long = 500, visibility: Int = View.INVISIBLE, completion: (() -> Unit)? = null) {
    animate()
        .alpha(0f) // Define o valor alfa para 0 (completamente transparente).
        .setDuration(duration) // Define a duração da animação.
        .withEndAction { // Define uma ação a ser executada após a animação.
            this.visibility = visibility // Define a visibilidade da view após a animação.
            completion?.let {
                it() // Executa a função de callback, se fornecida.
            }
        }
}

/**
 * Aplica uma animação de fade in (aparecimento gradual) à view.
 *
 * Uso:
 * // Fade in com duração padrão (500ms).
 * myView.fadeInAnimation()
 *
 * // Fade in com duração de 1 segundo.
 * myView.fadeInAnimation(duration = 1000)
 *
 * // Fade in com callback executado após a animação.
 * myView.fadeInAnimation {
 *     // Faça algo após a animação.
 * }
 * ```
 *
 * @param duration Duração da animação em milissegundos. Padrão: 500ms.
 * @param completion Função a ser executada após o término da animação (opcional).
 */
fun View.fadeInAnimation(duration: Long = 500, completion: (() -> Unit)? = null) {
    alpha = 0f // Define o valor alfa inicial para 0 (completamente transparente).
    visibility = View.VISIBLE // Define a visibilidade para VISIBLE antes da animação.
    animate()
        .alpha(1f) // Define o valor alfa para 1 (completamente opaco).
        .setDuration(duration) // Define a duração da animação.
        .withEndAction { // Define uma ação a ser executada após a animação.
            completion?.let {
                it() // Executa a função de callback, se fornecida.
            }
        }
}


/**
 * Define a largura da view em dp (Density-independent Pixels).
 * Útil para definir a largura da view de forma independente da densidade de pixels da tela.
 *
 * Uso:
 * myView.setWidthInDp(widthInDp = 100f)
 * ```
 *
 * @param widthInDp Largura da view em dp.
 */


/**
 * Define o estilo de fundo da view usando um GradientDrawable.
 * Permite definir a cor de fundo, raio dos cantos, espessura da borda e cor da borda.
 *
 * Uso:
 * // Fundo vermelho com cantos arredondados de 10dp, borda preta de 2dp.
 * myView.styleBackground(backgroundColor = "#FF0000", radius = 10f, borderWidth = 2, borderColor = "#000000")
 *
 * // Fundo transparente com cantos superiores arredondados de 15dp.
 * myView.styleBackground(backgroundColor = "#00000000", topLeftRadius = 15f, topRightRadius = 15f)
 * ```
 *
 * @param backgroundColor Cor de fundo em formato hexadecimal (ex: "#FF0000").
 *                        Padrão: transparente ("#00FFFFFF").
 * @param radius Raio dos cantos em pixels. Este valor será usado para todos os cantos, a menos que
 *               valores específicos sejam fornecidos para topLeftRadius, topRightRadius, bottomLeftRadius
 *               e bottomRightRadius. Padrão: 0f (sem cantos arredondados).
 * @param borderWidth Espessura da borda em pixels. Padrão: 0 (sem borda).
 * @param borderColor Cor da borda em formato hexadecimal. Padrão: transparente ("#00FFFFFF").
 * @param topLeftRadius Raio do canto superior esquerdo em pixels. Padrão: igual ao valor de `radius`.
 * @param topRightRadius Raio do canto superior direito em pixels. Padrão: igual ao valor de `radius`.
 * @param bottomLeftRadius Raio do canto inferior esquerdo em pixels. Padrão: igual ao valor de `radius`.
 * @param bottomRightRadius Raio do canto inferior direito em pixels. Padrão: igual ao valor de `radius`.
 */
fun View.styleBackground(
    backgroundColor: String ?= "#00FFFFFF",
    radius: Float = 0f,
    borderWidth: Int = 0,
    borderColor: String ?= "#00FFFFFF",
    topLeftRadius: Float = radius,
    topRightRadius: Float = radius,
    bottomLeftRadius: Float = radius,
    bottomRightRadius: Float = radius
) {
    // Cria um novo GradientDrawable.
    val shape = GradientDrawable()
    // Define o formato como retângulo.
    shape.shape = GradientDrawable.RECTANGLE
    // Define o raio dos cantos.
    shape.cornerRadii = floatArrayOf(
        topLeftRadius, topLeftRadius,
        topRightRadius, topRightRadius,
        bottomRightRadius, bottomRightRadius,
        bottomLeftRadius, bottomLeftRadius
    )
    // Define a cor de fundo.
    shape.setColor(Color.parseColor(backgroundColor))
    // Define a borda.
    shape.setStroke(borderWidth, Color.parseColor(borderColor))
    // Define o GradientDrawable como background da view.
    this.background = shape
}



fun View.styleBackground2(
    @DrawableRes svgDrawable: Int? = null,
    @ColorInt backgroundColor: Int = Color.TRANSPARENT,
    radius: Float = 0f,
    borderWidth: Int = 0,
    @ColorInt borderColor: Int = Color.TRANSPARENT,
    topLeftRadius: Float = radius,
    topRightRadius: Float = radius,
    bottomLeftRadius: Float = radius,
    bottomRightRadius: Float = radius
) {

    val background = GradientDrawable()
    background.cornerRadius = radius

    // Definindo o background
    background.setColor(backgroundColor)

    // Criando a borda
    if (borderWidth > 0) {
        val strokeDrawable = GradientDrawable()
        strokeDrawable.setStroke(borderWidth, borderColor)

        // Criando um LayerDrawable para combinar o SVG e a borda
        val layers = arrayOf<Drawable>(strokeDrawable, background)
        val layerDrawable = LayerDrawable(layers)

        // Definindo o SVG como background, se fornecido
        if (svgDrawable != null) {
            val vectorDrawable = ContextCompat.getDrawable(context, svgDrawable)
            vectorDrawable?.let {
                // Ajustar o tamanho do SVG conforme necessário
                it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)
                layerDrawable.setDrawableByLayerId(0, it)
            }
        }

        // Definindo o LayerDrawable como background da View
        this.background = layerDrawable
    } else {
        // Se não houver borda, defina apenas o background
        this.background = background
    }

    // Definindo os cantos arredondados individualmente
    background.cornerRadii = floatArrayOf(
        topLeftRadius, topLeftRadius,
        topRightRadius, topRightRadius,
        bottomRightRadius, bottomRightRadius,
        bottomLeftRadius, bottomLeftRadius
    )
}

/**
 * Define um gradiente de fundo para a view usando um GradientDrawable.
 * O gradiente é definido com duas cores e orientação diagonal (bottom-left para top-right).
 *
 * Uso:
 * // Gradiente de vermelho para azul.
 * myView.backgroundColorChangerGradient2(color1 = "#FF0000", color2 = "#0000FF")
 * ```
 *
 * @param color1 Primeira cor do gradiente em formato hexadecimal.
 * @param color2 Segunda cor do gradiente em formato hexadecimal.
 */
fun View.backgroundColorChangerGradient2(
    color1: String,
    color2: String,
) {
    // Define as cores do gradiente.
    val startColors = intArrayOf(
        Color.parseColor(color1),
        Color.parseColor(color2)
    )

    // Cria um novo GradientDrawable com orientação diagonal.
    val gradientDrawable = GradientDrawable(
        GradientDrawable.Orientation.BL_TR,
        startColors,
    )

    // Define o raio dos cantos (opcional).
    gradientDrawable.cornerRadius = 15f
    // Define o GradientDrawable como background da view.
    this.background = gradientDrawable
}

/**
 * Anima a escala de uma view para um determinado fator.
 * A view será escalada uniformemente nos eixos X e Y.
 *
 * Uso:
 * ```kotlin
 * val myView: View = findViewById(R.id.my_view)
 * myView.animateScale(1.08f) // Usa a duração padrão de 200ms
 * myView.animateScale(1.5f, 500) // Define a duração para 500ms
 * ```
 *
 * @param scaleFactor O fator de escala desejado.
 * @param duration A duração da animação em milissegundos (padrão: 200ms).
 */
fun View.animateScale(scaleFactor: Float, duration: Long = 200) {
    val scaleAnimator = ValueAnimator.ofFloat(scaleX, scaleFactor)
    scaleAnimator.duration = duration
    scaleAnimator.addUpdateListener { animation ->
        val value = animation.animatedValue as Float
        scaleX = value
        scaleY = value
    }
    scaleAnimator.start()
}





