package com.theporouscity.flagging.ilx;

import android.util.Log;

import org.simpleframework.xml.ElementList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bergstroml on 6/28/16.
 */

public class PollOptions {

    @ElementList(entry="Id",inline=true)
    private List<Integer> Ids;

    @ElementList(entry="OptionText",inline=true)
    private List<String> OptionTexts;

    private List<PollOption> PollOptions;

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

    public class PollOption {
        private int Id;
        private String OptionText;

        public PollOption(int id, String optionText) {
            Id = id;
            OptionText = optionText;
        }

        public int getId() { return Id; }
        public String getOptionText() { return OptionText; }

    }
}
