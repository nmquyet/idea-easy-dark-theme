package com.qyengyn.easydarktheme.java;

public class Test {

    public static Data1 from() {
        var dto = new Data1();
        return null;
    }

    public static class Data1 {
        private String id;
        private String name;
        private boolean isNew;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isNew() {
            return isNew;
        }

        public void setNew(boolean aNew) {
            isNew = aNew;
        }
    }
}
