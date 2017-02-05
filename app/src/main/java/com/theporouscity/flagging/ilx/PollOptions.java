package com.theporouscity.flagging.ilx;

import android.util.Log;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;
import org.simpleframework.xml.ElementList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bergstroml on 6/28/16.
 */

@Parcel
public class PollOptions {

    @ElementList(entry="Id",inline=true)
    List<Integer> Ids;

    @ElementList(entry="OptionText",inline=true)
    List<String> OptionTexts;

    List<PollOption> PollOptions;

    public List<Integer> getIds() { return Ids; }

    public List<String> getOptionTexts() { return OptionTexts; }

    public List<PollOption> getPollOptions() {

        if (PollOptions == null) {

            if (Ids.size() != OptionTexts.size()) {
                Log.d("PollOptions", "different number of ids and options");
                return null;
            }

            PollOptions = new ArrayList<PollOption>();

            for (int i = 0; i < Ids.size(); i++) {
                PollOptions.add(new PollOption(Ids.get(i), OptionTexts.get(i)));
            }

        }

        return PollOptions;
    }

    @Parcel
    public static class PollOption {
        int id;
        String optionText;

        @ParcelConstructor
        public PollOption(int id, String optionText) {
            this.id = id;
            this.optionText = optionText;
        }

        public int getId() { return id; }
        public String getOptionText() { return optionText; }

    }
}
