package com.alight.android.aoa_launcher.activity

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.common.base.BaseActivity
import com.alight.android.aoa_launcher.common.constants.AppConstants
import com.alight.android.aoa_launcher.presenter.PresenterImpl
import com.alight.android.aoa_launcher.ui.adapter.LauncherCenterAdapter
import kotlinx.android.synthetic.main.activity_launcher.*


/**
 * @author wangzhe
 * 新的Launcher主页
 */
class NewLauncherActivity : BaseActivity(), View.OnClickListener {

    private var launcherCenterAdapter: LauncherCenterAdapter? = null

    override fun setListener() {
    }


    override fun initData() {

    }

    override fun initView() {
        tv_ar_launcher.setOnClickListener(this)
        tv_chinese_launcher.setOnClickListener(this)
        tv_mathematics_launcher.setOnClickListener(this)
        tv_english_launcher.setOnClickListener(this)
        tv_quality_launcher.setOnClickListener(this)
    }

    override fun initPresenter(): PresenterImpl {
        return PresenterImpl()
    }

    override fun getLayout(): Int {
        return R.layout.activity_launcher
    }

    override fun onSuccess(any: Any) {
    }

    override fun onError(error: String) {
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    override fun onClick(v: View) {
        when (v.id) {
            R.id.tv_ar_launcher, R.id.tv_chinese_launcher, R.id.tv_mathematics_launcher, R.id.tv_english_launcher, R.id.tv_quality_launcher
            -> {
                showLeftSelectUI(v.id)
                showSelectUI(v.id)
            }
        }
    }

    private fun showSelectUI(id: Int) {
        when (id) {
            R.id.tv_ar_launcher -> {
                showArUI(true, AppConstants.LAUNCHER_TYPE_AR)
            }
            R.id.tv_chinese_launcher -> {
                showArUI(false, AppConstants.LAUNCHER_TYPE_CHINESE)
            }
            R.id.tv_mathematics_launcher -> {
                showArUI(false, AppConstants.LAUNCHER_TYPE_MATHEMATICS)
            }
            R.id.tv_english_launcher -> {
                showArUI(false, AppConstants.LAUNCHER_TYPE_ENGLISH)
            }
            R.id.tv_quality_launcher -> {
                showArUI(false, AppConstants.LAUNCHER_TYPE_QUALITY)
            }
        }
    }

    private fun showArUI(isShow: Boolean, launcherType: String) {
        //隐藏ar的UI
        ll_ar_launcher1.visibility = if (isShow) View.VISIBLE else View.GONE
        ll_ar_launcher2.visibility = if (isShow) View.VISIBLE else View.GONE
        ll_ar_launcher3.visibility = if (isShow) View.VISIBLE else View.GONE
        iv_ip_image.visibility = if (isShow) View.VISIBLE else View.GONE
        fl_center_launcher.visibility = if (isShow) View.GONE else View.VISIBLE
        fl_right_launcher.visibility = if (isShow) View.GONE else View.VISIBLE
        //不为Ar页面
        if (!isShow) {
            setAdapterUI(launcherType)
            if (launcherType == AppConstants.LAUNCHER_TYPE_QUALITY) {
                fl_center_launcher.setBackgroundResource(R.drawable.launcher_art_bg)
                fl_right_launcher.setBackgroundResource(R.drawable.launcher_think_bg)
            } else {
                fl_center_launcher.setBackgroundResource(R.drawable.launcher_syn_learn_bg)
                fl_right_launcher.setBackgroundResource(R.drawable.launcher_instruction_after_class_bg)
            }
        }
    }

    private fun setAdapterUI(launcherType: String) {
        if (launcherCenterAdapter == null) {
            launcherCenterAdapter = LauncherCenterAdapter()
            rv_center_launcher.layoutManager = GridLayoutManager(this, 3)
            rv_center_launcher.adapter = launcherCenterAdapter
        }
        launcherCenterAdapter?.setShowType(launcherType)

    }

    private fun showLeftSelectUI(id: Int) {
        iv_ar_launcher.visibility = if (id == R.id.tv_ar_launcher) View.VISIBLE else View.GONE
        iv_chinese_launcher.visibility =
            if (id == R.id.tv_chinese_launcher) View.VISIBLE else View.GONE
        iv_mathematics_launcher.visibility =
            if (id == R.id.tv_mathematics_launcher) View.VISIBLE else View.GONE
        iv_english_launcher.visibility =
            if (id == R.id.tv_english_launcher) View.VISIBLE else View.GONE
        iv_quality_launcher.visibility =
            if (id == R.id.tv_quality_launcher) View.VISIBLE else View.GONE
    }


}
