package cn.com.billboard.ui.fragment;


import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.BindView;
import cn.com.billboard.R;
import cn.com.billboard.model.ProgressModel;
import cn.com.library.event.BusProvider;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class FragmentUpdate extends BaseFragment {


    private int file_pre;
    private String file_num = "";
    private String file_name = "";
    private boolean isUPdate = true;

    @BindView(R.id.progressBarHorizontal)
    ProgressBar progressBarHorizontal;
    @BindView(R.id.loading_file_name)
    TextView loading_file_name;
    @BindView(R.id.loading_num)
    TextView loading_num;
    @BindView(R.id.loading_pro)
    TextView loading_pro;

    private Handler mHandler = new Handler();



    @Override
    public int getLayoutId() {
        return R.layout.fragment_update;
    }

    @Override
    protected void init() {
        BusProvider.getBus().toFlowable(ProgressModel.class).observeOn(AndroidSchedulers.mainThread()).subscribe(
                progressModel -> {

                    int pp = (int) ((float) progressModel.progress / (float) progressModel.total * 100);
                    file_pre = pp;
                    file_num = progressModel.index + "/" + progressModel.num;
                    file_name = progressModel.type + progressModel.fileName;
                    if (isUPdate) {
                        isUPdate = false;
                        mHandler.postDelayed(runnable, 30);
                    }
                }
        );
    }

    Runnable runnable =  new Runnable() {
        @Override
        public void run()
        {
            loading_file_name.setText(file_name);
            loading_num.setText(file_num);
            progressBarHorizontal.setProgress(file_pre);
            loading_pro.setText(file_pre+"%");
            mHandler.postDelayed(runnable, 100);
        }
    };



}
