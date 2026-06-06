/* By Ly Minh Hoang */
package com.rotaguard.jdbc.model;

public class Staff {
  private String maNv;
  private String hoTen;
  private String khoa;
  private double heSo;
  private String vaiTro;
  private String sdt;
  private String email;
  private int namKn;

  public Staff() {}

  public Staff(
      String maNv,
      String hoTen,
      String khoa,
      double heSo,
      String vaiTro,
      String sdt,
      String email,
      int namKn) {
    this.maNv = maNv;
    this.hoTen = hoTen;
    this.khoa = khoa;
    this.heSo = heSo;
    this.vaiTro = vaiTro;
    this.sdt = sdt;
    this.email = email;
    this.namKn = namKn;
  }

  public String getMaNv() {
    return maNv;
  }

  public void setMaNv(String maNv) {
    this.maNv = maNv;
  }

  public String getHoTen() {
    return hoTen;
  }

  public void setHoTen(String hoTen) {
    this.hoTen = hoTen;
  }

  public String getKhoa() {
    return khoa;
  }

  public void setKhoa(String khoa) {
    this.khoa = khoa;
  }

  public double getHeSo() {
    return heSo;
  }

  public void setHeSo(double heSo) {
    this.heSo = heSo;
  }

  public String getVaiTro() {
    return vaiTro;
  }

  public void setVaiTro(String vaiTro) {
    this.vaiTro = vaiTro;
  }

  public String getSdt() {
    return sdt;
  }

  public void setSdt(String sdt) {
    this.sdt = sdt;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public int getNamKn() {
    return namKn;
  }

  public void setNamKn(int namKn) {
    this.namKn = namKn;
  }
}
