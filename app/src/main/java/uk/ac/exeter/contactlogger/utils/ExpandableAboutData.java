package uk.ac.exeter.contactlogger.utils;

import android.content.Context;
import android.text.Html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uk.ac.exeter.contactlogger.R;

/**
 * Created by apps4research on 2015-11-12.
 */
public class ExpandableAboutData {

    public static HashMap<String, List<CharSequence>> getData(Context context) {
        HashMap<String, List<CharSequence>> expandableListDetail = new HashMap<>();

        //array for content
        List<CharSequence> contact = new ArrayList<>();
        contact.add(Html.fromHtml(context.getString(R.string.about_contact)));

        List<CharSequence> faq = new ArrayList<>();
        faq.add(Html.fromHtml(context.getString(R.string.about_faq1)));
        faq.add(Html.fromHtml(context.getString(R.string.about_faq2)));

        List<CharSequence> project = new ArrayList<>();
        project.add(Html.fromHtml(context.getString(R.string.about_project)));

        List<CharSequence> credits = new ArrayList<>();
        credits.add(Html.fromHtml(context.getString(R.string.about_credits)));

        List<CharSequence> copyright = new ArrayList<>();
        copyright.add(Html.fromHtml(context.getString(R.string.about_copyright)));

        //combine headlines with content and put into list
        expandableListDetail.put(context.getString(R.string.about_project_hl), project);
        expandableListDetail.put(context.getString(R.string.about_faq_hl), faq);
        expandableListDetail.put(context.getString(R.string.about_contact_hl), contact);
        expandableListDetail.put(context.getString(R.string.about_credits_hl), credits);
        expandableListDetail.put(context.getString(R.string.about_copyright_hl), copyright);

        return expandableListDetail;
    }
}
