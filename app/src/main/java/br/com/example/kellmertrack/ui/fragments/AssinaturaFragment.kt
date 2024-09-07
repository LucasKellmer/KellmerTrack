package br.com.example.kellmertrack.ui.fragments

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import br.com.example.kellmertrack.R
import br.com.example.kellmertrack.databinding.AssinaturaFragmentBinding
import br.com.example.kellmertrack.ui.viewmodel.AppViewModel
import br.com.example.kellmertrack.ui.viewmodel.ComponentesFragments
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

@AndroidEntryPoint
class AssinaturaFragment : Fragment() {

    private val appViewModel: AppViewModel by activityViewModels()
    private val routerController by lazy { findNavController() }
    private lateinit var binding: AssinaturaFragmentBinding
    val args : AssinaturaFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.assinatura_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = AssinaturaFragmentBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            val signatureBase64 = savedInstanceState.getString("signatureBitmapBase64")
            if (signatureBase64 != null) {
                val bitmap = base64ToBitmap(signatureBase64)
                binding.vAssinatura.setSignatureBitmap(bitmap)
            }
        }
        configuraBotoes()
    }

    override fun onResume() {
        super.onResume()
        appViewModel.setBottomBar(ComponentesFragments(bottomBar = false))
    }

    private fun configuraBotoes() {
        binding.btnSave.setOnClickListener {
            val bitmap = binding.vAssinatura.transparentSignatureBitmap
            val bitmapString = bitmapToBase64(bitmap)
            args.entregaDTO.assinatura = bitmapString//binding.vAssinatura.transparentSignatureBitmap
            val bundle = Bundle().apply {
                putParcelable("entregaDTO", args.entregaDTO)
                putString("comprovante", "NOVO")
            }
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            routerController.navigate(R.id.action_assinaturaFragment_to_ComprovanteFragment, bundle)
        }

        binding.btnClear.setOnClickListener {
            binding.vAssinatura.clear()
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun base64ToBitmap(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeStream(ByteArrayInputStream(decodedBytes))
    }
}