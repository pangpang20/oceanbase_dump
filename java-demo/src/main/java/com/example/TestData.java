package com.example;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * test01 表的数据模型类
 */
public class TestData {
    private Long id;
    private String name;
    private String code;
    private Integer age;
    private BigDecimal salary;
    private Double balance;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDate birthDate;

    public TestData() {}

    public TestData(Long id, String name, String code, Integer age, BigDecimal salary,
                    Double balance, Integer status, LocalDateTime createTime,
                    LocalDateTime updateTime, LocalDate birthDate) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.age = age;
        this.salary = salary;
        this.balance = balance;
        this.status = status;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.birthDate = birthDate;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }
    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    @Override
    public String toString() {
        return "TestData{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", age=" + age +
                ", salary=" + salary +
                ", balance=" + balance +
                ", status=" + status +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", birthDate=" + birthDate +
                '}';
    }
}
