package org.lntu.online.ui.fragment;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lntu.online.R;

import org.joda.time.LocalDate;
import org.lntu.online.model.entity.ClassTable;
import org.lntu.online.ui.adapter.ClassTablePageAdapter;
import org.lntu.online.ui.base.ClassTableFragment;
import org.lntu.online.ui.dialog.ClassTableTimeDialogHolder;

import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ClassTablePageFragment extends ClassTableFragment {

    @InjectView(R.id.class_table_page_view_pager)
    protected ViewPager viewPager;

    private ClassTablePageAdapter adapter;

    private ClassTableTimeDialogHolder holder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_class_table_page, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);

        holder = new ClassTableTimeDialogHolder(getActivity());
        holder.setOnTimeDialogFinishListener(new ClassTableTimeDialogHolder.OnDialogFinishListener() {

            @Override
            public void onDialogFinish(LocalDate currentDate) {
                if (adapter != null) {
                    viewPager.setCurrentItem(adapter.getPositionFromDate(currentDate), true);
                }
            }

        });
    }

    @Override
    public void onDataSetInit(int year, String term, LocalDate today) {
        adapter = new ClassTablePageAdapter(getActivity(), year, term, today);
        viewPager.setAdapter(adapter);
        String currentTerm = (today.getMonthOfYear() >= 2 && today.getMonthOfYear() < 8) ? "春" : "秋";
        if (today.getYear() == year && currentTerm.equals(term)) {
            viewPager.setCurrentItem(adapter.getPositionFromDate(today), true);
        } else if (term.equals("春")) {
            viewPager.setCurrentItem(adapter.getPositionFromDate(new LocalDate(year, 3, 1)), true);
        } else {
            viewPager.setCurrentItem(adapter.getPositionFromDate(new LocalDate(year, 9, 1)), true);
        }
    }

    @Override
    public void onDataSetUpdate(ClassTable classTable, Map<String, List<ClassTable.CourseWrapper>> classTableMap) {
        adapter.updateDataSet(classTable, classTableMap);
    }

    public void onSetToday() {
        if (adapter != null) {
            holder.showDialog(adapter.getDateAt(viewPager.getCurrentItem()));
        }
    }

}
