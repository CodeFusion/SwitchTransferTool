package ca.codefusion.switchtransfertool.ui.transfer.tutorial

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ca.codefusion.switchtransfertool.databinding.TutorialPageFragmentBinding

class TutorialPagePermissionFragment(
    private val image: Drawable?,
    private val bodyText: String,
    private val permissions: Array<String>,
    private val rationale: String,
    private val imageDesc: String? = null
): Fragment() {

    private lateinit var binding: TutorialPageFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TutorialPageFragmentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (getView() == null) {
            return
        }

        val ivImage = binding.slideImage
        val tvBody = binding.slideBody

        ivImage.setImageDrawable(image)
        ivImage.contentDescription = imageDesc
        tvBody.text = bodyText




    }
}