package com.ellalee.travelmaker;

public class MarkerTag{
    public int icon;
    public final long dbId;

    MarkerTag(int i,long id){
        icon =i;
        dbId=id;
    }
    public long getId(){
        return dbId;
    }
    public int getIcon(){
        return icon;
    }
    public void setIcon(int i){
        icon = i;
    }

    @Override
    public int hashCode() { //getIcon
        return icon;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj.equals(dbId))? true : false;
        //return super.equals(obj);
    }
    @Override
    public String toString() { //getId
        return String.valueOf(dbId);
//            return "ICON: "+icon+", ID: "+dbId;
    }
}