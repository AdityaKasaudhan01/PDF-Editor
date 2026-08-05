package com.pdfwordeditor.app.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "text_runs")
public class TextRunEntity {

  @Id
  private String id;

  @ManyToOne
  private PageEntity page;

  private String text;
  private String fontFamily;
  private double fontSize;
  private String fontWeight;
  private String fontStyle;
  private String color;
  private double x;
  private double y;
  private double width;
  private double height;
  private double rotation;

  public TextRunEntity() {
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public PageEntity getPage() {
    return page;
  }

  public void setPage(PageEntity page) {
    this.page = page;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getFontFamily() {
    return fontFamily;
  }

  public void setFontFamily(String fontFamily) {
    this.fontFamily = fontFamily;
  }

  public double getFontSize() {
    return fontSize;
  }

  public void setFontSize(double fontSize) {
    this.fontSize = fontSize;
  }

  public String getFontWeight() {
    return fontWeight;
  }

  public void setFontWeight(String fontWeight) {
    this.fontWeight = fontWeight;
  }

  public String getFontStyle() {
    return fontStyle;
  }

  public void setFontStyle(String fontStyle) {
    this.fontStyle = fontStyle;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public double getX() {
    return x;
  }

  public void setX(double x) {
    this.x = x;
  }

  public double getY() {
    return y;
  }

  public void setY(double y) {
    this.y = y;
  }

  public double getWidth() {
    return width;
  }

  public void setWidth(double width) {
    this.width = width;
  }

  public double getHeight() {
    return height;
  }

  public void setHeight(double height) {
    this.height = height;
  }

  public double getRotation() {
    return rotation;
  }

  public void setRotation(double rotation) {
    this.rotation = rotation;
  }
}
