package com.softwell.softwell.service;

import com.softwell.softwell.model.Testimonio;
import com.softwell.softwell.repository.TestimonioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TestimonioService {

    private final TestimonioRepository testimonioRepository;

    public TestimonioService(TestimonioRepository testimonioRepository) {
        this.testimonioRepository = testimonioRepository;
    }

    public List<Testimonio> findAll() {
        return testimonioRepository.findAll();
    }

    public List<Testimonio> findVisibles() {
        return testimonioRepository.findByVisible(true);
    }

    public Optional<Testimonio> findById(String id) {
        return testimonioRepository.findById(id);
    }

    public Testimonio save(Testimonio testimonio) {
        return testimonioRepository.save(testimonio);
    }

    public void deleteById(String id) {
        testimonioRepository.deleteById(id);
    }
}