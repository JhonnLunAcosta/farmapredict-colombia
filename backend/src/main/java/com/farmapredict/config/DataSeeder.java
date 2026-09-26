package com.farmapredict.config;

import com.farmapredict.model.Inventario;
import com.farmapredict.model.Medicamento;
import com.farmapredict.model.SismedPrecio;
import com.farmapredict.model.Usuario;
import com.farmapredict.repository.InventarioRepository;
import com.farmapredict.repository.MedicamentoRepository;
import com.farmapredict.repository.SismedPrecioRepository;
import com.farmapredict.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seed(MedicamentoRepository meds, InventarioRepository inv,
                           UsuarioRepository users, SismedPrecioRepository precios,
                           PasswordEncoder encoder) {
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
            if (meds.count() == 0) {
                // Catálogo base de críticos (estructura CUM INVIMA: código, principio activo, titular, estado).
                // Fuente maestro: app.invima.gov.co/cum + datos.gov.co. Ampliar con POST /api/catalogo/importar.
                String[][] cat = {
                    {"N05BA01", "Norepinefrina 4mg/4mL", "4mg/4mL", "UCI", "NOREPINEFRINA", "LABORATORIOS GENERICOS", "VIGENTE", "INVIMA 2021M-0001"},
                    {"A10BA02", "Metformina 850mg", "850mg", "CRONICO", "METFORMINA", "LABORATORIOS GENERICOS", "VIGENTE", "INVIMA 2020M-0002"},
                    {"N02BE01", "Acetaminofén 500mg", "500mg", "GENERAL", "ACETAMINOFEN", "LABORATORIOS GENERICOS", "VIGENTE", "INVIMA 2019M-0003"},
                    {"C09AA05", "Enalapril 10mg", "10mg", "CRONICO", "ENALAPRIL", "LABORATORIOS GENERICOS", "VIGENTE", "INVIMA 2020M-0004"},
                    {"J01CA04", "Amoxicilina 500mg", "500mg", "ANTIBIOTICO", "AMOXICILINA", "LABORATORIOS GENERICOS", "VIGENTE", "INVIMA 2021M-0005"},
                    {"J01DD04", "Ceftriaxona 1g", "1g", "ANTIBIOTICO", "CEFTRIAXONA", "LABORATORIOS GENERICOS", "VIGENTE", "INVIMA 2021M-0006"},
                    {"B05AA01", "Albúmina 20% x 50mL", "20%", "UCI", "ALBUMINA HUMANA", "LABORATORIOS GENERICOS", "VIGENTE", "INVIMA 2022M-0007"},
                    {"N03AX14", "Levetiracetam 500mg", "500mg", "NEURO", "LEVETIRACETAM", "LABORATORIOS GENERICOS", "VIGENTE", "INVIMA 2022M-0008"},
                    {"L01AA01", "Ciclofosfamida 500mg", "500mg", "ONCO", "CICLOFOSFAMIDA", "LABORATORIOS GENERICOS", "VIGENTE", "INVIMA 2021M-0009"},
                    {"J05AR06", "Dolutegravir/Lamivudina/Tenofovir", "50/300/300mg", "VIH", "DOLUTEGRAVIR+LAMIVUDINA+TENOFOVIR", "LABORATORIOS GENERICOS", "VIGENTE", "INVIMA 2023M-0010"},
                    {"N03AG01", "Ácido valproico 250mg", "250mg", "NEURO", "ACIDO VALPROICO", "LABORATORIOS GENERICOS", "VIGENTE", "INVIMA 2020M-0011"},
                    {"A10AB01", "Insulina glargina 100UI/mL", "100UI/mL", "CRONICO", "INSULINA GLARGINA", "LABORATORIOS GENERICOS", "VIGENTE", "INVIMA 2022M-0012"},
                    {"C07AB03", "Atenolol 50mg", "50mg", "CRONICO", "ATENOLOL", "LABORATORIOS GENERICOS", "VIGENTE", "INVIMA 2020M-0013"},
                    {"J01MA02", "Ciprofloxacina 500mg", "500mg", "ANTIBIOTICO", "CIPROFLOXACINA", "LABORATORIOS GENERICOS", "VIGENTE", "INVIMA 2021M-0014"},
                    {"N05AH03", "Olanzapina 10mg", "10mg", "NEURO", "OLANZAPINA", "LABORATORIOS GENERICOS", "VIGENTE", "INVIMA 2022M-0015"},
                    {"B01AC06", "Ácido acetilsalicílico 100mg", "100mg", "CRONICO", "ACIDO ACETILSALICILICO", "LABORATORIOS GENERICOS", "VIGENTE", "INVIMA 2019M-0016"},
                };
                for (String[] r : cat) {
                    meds.save(new Medicamento(r[0], r[1], r[2], r[3], r[4], r[5], r[6], r[7]));
                }
                Object[][] stock = {
                    {"N05BA01", "UCI Central", 180, 120.0}, {"A10BA02", "Bogotá Sur", 900, 750.0},
                    {"N02BE01", "Bogotá Norte", 1200, 800.0}, {"C09AA05", "Bogotá Norte", 5000, 900.0},
                    {"J01CA04", "Bogotá Sur", 3000, 1100.0}, {"J01DD04", "UCI Central", 420, 200.0},
                    {"B05AA01", "UCI Central", 60, 45.0}, {"N03AX14", "Bogotá Sur", 2500, 400.0},
                    {"L01AA01", "UCI Central", 90, 60.0}, {"J05AR06", "Bogotá Sur", 1500, 300.0},
                    {"N03AG01", "Bogotá Sur", 1100, 500.0}, {"A10AB01", "Bogotá Norte", 700, 420.0},
                };
                for (Object[] s : stock) {
                    meds.findByCodigo((String) s[0]).ifPresent(m ->
                        inv.save(new Inventario(m, (String) s[1], (Integer) s[2], (Double) s[3])));
                }
                // Precios de referencia de ejemplo (estructura SISMED 2026-T3, canal INS).
                // Dato real: SISPRO/SISMED público (web.sispro.gov.co) + minsalud.gov.co.
                Object[][] pr = {
                    {"N05BA01", "2026-T3", "INS", 18500.0, 22000.0, 19800.0, 12000L},
                    {"A10BA02", "2026-T3", "INS", 850.0, 1100.0, 930.0, 85000L},
                    {"J01DD04", "2026-T3", "INS", 9200.0, 12500.0, 10400.0, 9000L},
                };
                for (Object[] r : pr) {
                    SismedPrecio p = new SismedPrecio();
                    p.setCodigoCum((String) r[0]);
                    p.setPeriodo((String) r[1]);
                    p.setCanal((String) r[2]);
                    p.setPrecioMin((Double) r[3]);
                    p.setPrecioMax((Double) r[4]);
                    p.setPrecioProm((Double) r[5]);
                    p.setUnidades((Long) r[6]);
                    precios.save(p);
                }
            }
        };
    }
}
