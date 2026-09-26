package com.farmapredict.config;

import com.farmapredict.model.Inventario;
import com.farmapredict.model.Medicamento;
import com.farmapredict.model.Usuario;
import com.farmapredict.repository.InventarioRepository;
import com.farmapredict.repository.MedicamentoRepository;
import com.farmapredict.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seed(MedicamentoRepository meds, InventarioRepository inv,
                           UsuarioRepository users, PasswordEncoder encoder) {
        return args -> {
            if (users.count() == 0) {
                Usuario admin = new Usuario();
                admin.setUsername("admin");
                admin.setPassword(encoder.encode("admin123"));
                admin.setRol("ADMIN");
                users.save(admin);

                Usuario qf = new Usuario();
                qf.setUsername("qf");
                qf.setPassword(encoder.encode("qf123"));
                qf.setRol("QF");
                users.save(qf);

                Usuario aux = new Usuario();
                aux.setUsername("aux");
                aux.setPassword(encoder.encode("aux123"));
                aux.setRol("USER");
                users.save(aux);
            }
            if (meds.count() > 0) return;
            Medicamento m1 = meds.save(new Medicamento("N05BA01", "Norepinefrina 4mg/4mL", "4mg/4mL", "UCI"));
            Medicamento m2 = meds.save(new Medicamento("A10BA02", "Metformina 850mg", "850mg", "CRONICO"));
            Medicamento m3 = meds.save(new Medicamento("N02BE01", "Acetaminofén 500mg", "500mg", "GENERAL"));
            Medicamento m4 = meds.save(new Medicamento("C09AA05", "Enalapril 10mg", "10mg", "CRONICO"));
            Medicamento m5 = meds.save(new Medicamento("J01CA04", "Amoxicilina 500mg", "500mg", "ANTIBIOTICO"));
            Medicamento m6 = meds.save(new Medicamento("J01DD04", "Ceftriaxona 1g", "1g", "ANTIBIOTICO"));
            Medicamento m7 = meds.save(new Medicamento("B05AA01", "Albúmina 20%", "20%", "UCI"));
            Medicamento m8 = meds.save(new Medicamento("N03AX14", "Levetiracetam 500mg", "500mg", "NEURO"));
            inv.save(new Inventario(m1, "UCI Central", 180, 120.0));
            inv.save(new Inventario(m2, "Bogotá Sur", 900, 750.0));
            inv.save(new Inventario(m3, "Bogotá Norte", 1200, 800.0));
            inv.save(new Inventario(m4, "Bogotá Norte", 5000, 900.0));
            inv.save(new Inventario(m5, "Bogotá Sur", 3000, 1100.0));
            inv.save(new Inventario(m6, "UCI Central", 420, 200.0));
            inv.save(new Inventario(m7, "UCI Central", 60, 45.0));
            inv.save(new Inventario(m8, "Bogotá Sur", 2500, 400.0));
        };
    }
}
