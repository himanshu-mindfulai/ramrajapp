package com.mindfulai.Models.kwikapimodels;

import java.util.ArrayList;

public class PlansResponse {
    private ArrayList<PlanItem> FULLTT;
    private ArrayList<PlanItem> TOPUP;
    private ArrayList<PlanItem> DATA;
    private ArrayList<PlanItem> SMS;
    private ArrayList<PlanItem> Romaing;
    private ArrayList<PlanItem> FRC;
    private ArrayList<PlanItem> STV;

    public ArrayList<PlanItem> getFULLTT() {
        return FULLTT;
    }

    public ArrayList<PlanItem> getTOPUP() {
        return TOPUP;
    }

    public ArrayList<PlanItem> getDATA() {
        return DATA;
    }

    public ArrayList<PlanItem> getSMS() {
        return SMS;
    }

    public ArrayList<PlanItem> getRomaing() {
        return Romaing;
    }

    public ArrayList<PlanItem> getFRC() {
        return FRC;
    }

    public ArrayList<PlanItem> getSTV() {
        return STV;
    }
}
