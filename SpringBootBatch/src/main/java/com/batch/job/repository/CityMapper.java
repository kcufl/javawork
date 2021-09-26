package com.batch.job.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.batch.job.model.City;

@Mapper
public interface CityMapper {
    City selectCityById(Long id);
    List<City> selectAllCity();
    void insertCity(City city);
}
