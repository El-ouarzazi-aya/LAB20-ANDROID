package com.example.numberbook;

public class Contact {
    private int id;
    private String name;
    private String phone;
    private String source;
    private String created_at;

    public Contact() {}

    public Contact(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public int getId()             { return id; }
    public String getName()        { return name; }
    public String getPhone()       { return phone; }
    public String getSource()      { return source; }
    public String getCreated_at()  { return created_at; }

    public void setId(int id)                   { this.id = id; }
    public void setName(String name)            { this.name = name; }
    public void setPhone(String phone)          { this.phone = phone; }
    public void setSource(String source)        { this.source = source; }
    public void setCreated_at(String created_at){ this.created_at = created_at; }
}