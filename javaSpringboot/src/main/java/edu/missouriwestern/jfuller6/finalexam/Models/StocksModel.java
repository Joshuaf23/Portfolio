package edu.missouriwestern.jfuller6.finalexam.Models;

import jakarta.persistence.*;

@Entity
@Table(name="stocks")
public class StocksModel {
    @Id @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;
    @Column
    private Integer comments;
    @Column
    private String sentiment;
    @Column
    private Float score;
    @Column
    private String symbol;

    public StocksModel(Integer comments, String sentiment, Float score, String symbol){
        setComments(comments);
        setSentiment(sentiment);
        setScore(score);
        setSymbol(symbol);
    }

    public StocksModel(){
        this(0, "Unknown", 0.0F, "NA");
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getComments() {
        return comments;
    }

    public void setComments(Integer comments) {
        this.comments = comments;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
