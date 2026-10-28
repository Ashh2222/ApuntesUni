package com.university.website.config;

import com.university.website.model.*;
import com.university.website.model.enums.*;
import com.university.website.repository.*;
import com.university.website.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final AcademicProgramRepository programRepository;
    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;
    private final StudentProgramRepository studentProgramRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final NoteRepository noteRepository;
    private final RatingRepository ratingRepository;
    private final PasswordEncoder passwordEncoder;
    private final StorageService storageService;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        storageService.init();

        if (userRepository.count() > 0) {
            log.info("Base de datos ya inicializada con usuarios. Omitiendo seed.");
            return;
        }

        log.info("Iniciando seed de datos iniciales para Apuntes UNI...");

        // 1. Usuarios del sistema
        User admin = new User(
                "Administrador UNI",
                "admin@uni.edu.pe",
                passwordEncoder.encode("Password123!"),
                UserRole.ADMINISTRATOR
        );
        userRepository.save(admin);

        User moderador = new User(
                "Moderador Académico",
                "moderador@uni.edu.pe",
                passwordEncoder.encode("Password123!"),
                UserRole.MODERATOR
        );
        userRepository.save(moderador);

        Student studentAna = new Student();
        studentAna.setFullName("Ana Romero");
        studentAna.setEmail("ana@uni.edu.pe");
        studentAna.setPasswordHash(passwordEncoder.encode("Password123!"));
        studentAna.setUserRole(UserRole.STUDENT);
        studentAna.setStudentCode("20210045A");
        studentAna.setInstitutionalEmail("aromero@uni.edu.pe");
        studentAna.setEntryYear(2021);
        studentAna.setReputationPoints(150);
        studentAna.setVerifiedStudent(true);
        studentAna.setActive(true);
        studentAna.setBio("Estudiante de 6to ciclo de Ingeniería de Sistemas interesada en desarrollo de software y computación distribuida.");
        studentRepository.save(studentAna);

        Student studentCarlos = new Student();
        studentCarlos.setFullName("Carlos Mendoza");
        studentCarlos.setEmail("carlos@uni.edu.pe");
        studentCarlos.setPasswordHash(passwordEncoder.encode("Password123!"));
        studentCarlos.setUserRole(UserRole.STUDENT);
        studentCarlos.setStudentCode("20201123B");
        studentCarlos.setInstitutionalEmail("cmendoza@uni.edu.pe");
        studentCarlos.setEntryYear(2020);
        studentCarlos.setReputationPoints(320);
        studentCarlos.setVerifiedStudent(true);
        studentCarlos.setActive(true);
        studentCarlos.setBio("Amante de las matemáticas y la física teórica.");
        studentRepository.save(studentCarlos);

        // 2. Carreras / Programas Académicos
        AcademicProgram sis = new AcademicProgram();
        sis.setCode("SIS");
        sis.setName("Ingeniería de Sistemas");
        sis.setDescription("Carrera enfocada en tecnologías de la información, arquitectura de software y computación.");
        sis.setFaculty("Facultad de Ingeniería Industrial y de Sistemas");
        sis.setTotalSemesters(10);
        sis.setType(ProgramType.DEGREE);
        sis.setActive(true);
        programRepository.save(sis);

        AcademicProgram ind = new AcademicProgram();
        ind.setCode("IND");
        ind.setName("Ingeniería Industrial");
        ind.setDescription("Optimización de procesos, gestión de operaciones y cadena de suministro.");
        ind.setFaculty("Facultad de Ingeniería Industrial y de Sistemas");
        ind.setTotalSemesters(10);
        ind.setType(ProgramType.DEGREE);
        ind.setActive(true);
        programRepository.save(ind);

        AcademicProgram civ = new AcademicProgram();
        civ.setCode("CIV");
        civ.setName("Ingeniería Civil");
        civ.setDescription("Diseño, construcción y mantenimiento de infraestructuras.");
        civ.setFaculty("Facultad de Ingeniería Civil");
        civ.setTotalSemesters(10);
        civ.setType(ProgramType.DEGREE);
        civ.setActive(true);
        programRepository.save(civ);

        AcademicProgram mec = new AcademicProgram();
        mec.setCode("MEC");
        mec.setName("Ingeniería Mecánica");
        mec.setDescription("Diseño de sistemas mecánicos, térmicos y de fluidos.");
        mec.setFaculty("Facultad de Ingeniería Mecánica");
        mec.setTotalSemesters(10);
        mec.setType(ProgramType.DEGREE);
        mec.setActive(true);
        programRepository.save(mec);

        // 3. Materias
        Subject calculo1 = createSubject("MA101", "Cálculo I", "Límites, derivadas, integrales y aplicaciones del cálculo.", 4, Set.of(sis, ind, civ, mec));
        Subject fisica1 = createSubject("FI101", "Física I", "Mecánica clásica, cinemática, leyes de Newton y dinámica.", 4, Set.of(sis, ind, civ, mec));
        Subject algebra = createSubject("MA102", "Álgebra Lineal", "Matrices, determinantes, sistemas lineales y transformaciones.", 3, Set.of(sis, ind, civ, mec));
        Subject quimica = createSubject("QU101", "Química General", "Estructura atómica, enlaces químicos y estequiometría.", 3, Set.of(ind, civ, mec));
        Subject programacion1 = createSubject("CS101", "Programación I", "Fundamentos de programación, algoritmos y estructuras de datos.", 4, Set.of(sis));
        Subject estadistica = createSubject("MA201", "Estadística", "Probabilidades, variables aleatorias e inferencia estadística.", 3, Set.of(sis, ind));
        Subject circuitos = createSubject("EL101", "Circuitos Eléctricos", "Leyes de Kirchhoff, análisis de circuitos en régimen transitorio.", 4, Set.of(mec));
        Subject resistencia = createSubject("CI201", "Resistencia de Materiales", "Esfuerzo, deformación, flexión y torsión.", 4, Set.of(civ, mec));

        // 4. Temas para materias clave
        Topic topicIntegral = new Topic("TOP-01", "Integral definida", "Cálculo de áreas, volumen y teoremas fundamentales.", 3, calculo1);
        Topic topicLimites = new Topic("TOP-02", "Límites y continuidad", "Definición rigurosa, indeterminaciones y límites laterales.", 1, calculo1);
        Topic topicDerivadas = new Topic("TOP-03", "Derivadas y aplicaciones", "Reglas de derivación y optimización.", 2, calculo1);
        topicRepository.saveAll(List.of(topicIntegral, topicLimites, topicDerivadas));

        Topic topicNewton = new Topic("TOP-04", "Dinámica y leyes de Newton", "Fuerzas, equilibrio y segunda ley.", 2, fisica1);
        Topic topicCinematica = new Topic("TOP-05", "Cinemática", "MRU, MRUV y movimiento en dos dimensiones.", 1, fisica1);
        topicRepository.saveAll(List.of(topicNewton, topicCinematica));

        Topic topicEspacios = new Topic("TOP-06", "Transformaciones lineales y subespacios", "Núcleo, imagen y matriz de transformación.", 3, algebra);
        topicRepository.save(topicEspacios);

        Topic topicPOO = new Topic("TOP-07", "Programación Orientada a Objetos", "Clases, herencia, polimorfismo y encapsulamiento.", 3, programacion1);
        topicRepository.save(topicPOO);

        // 5. Vincular programas a estudiantes
        StudentProgram spAna = new StudentProgram(studentAna, sis, true, 6);
        studentProgramRepository.save(spAna);

        StudentProgram spCarlos = new StudentProgram(studentCarlos, sis, true, 8);
        studentProgramRepository.save(spCarlos);

        // 6. Inscripciones de Ana
        enrollStudentInSubject(studentAna, calculo1, "2025-2");
        enrollStudentInSubject(studentAna, fisica1, "2025-2");
        enrollStudentInSubject(studentAna, algebra, "2025-2");
        enrollStudentInSubject(studentAna, programacion1, "2025-2");
        enrollStudentInSubject(studentAna, estadistica, "2025-2");

        // 7. Archivos de muestra y Notas
        byte[] dummyPdfContent = "%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj 2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj 3 0 obj<</Type/Page/MediaBox[0 0 612 792]/Parent 2 0 R/Resources<<>>>>endobj\nxref\n0 4\n0000000000 65535 f\n0000000009 00000 n\n0000000052 00000 n\n0000000101 00000 n\ntrailer<</Size 4/Root 1 0 R>>\nstartxref\n180\n%%EOF".getBytes(StandardCharsets.ISO_8859_1);

        String fileKey1 = storageService.store(createDummyMultipartFile("resumen_integral_definida.pdf", dummyPdfContent));
        String fileKey2 = storageService.store(createDummyMultipartFile("guia_newton_fisica.pdf", dummyPdfContent));
        String fileKey3 = storageService.store(createDummyMultipartFile("transformaciones_lineales.pdf", dummyPdfContent));

        Note note1 = new Note(
                "Resumen completo: Integral definida con ejercicios resueltos",
                "Apuntes completos y detallados con más de 20 ejercicios resueltos paso a paso sobre el Teorema Fundamental del Cálculo y áreas bajo la curva.",
                "resumen_integral_definida.pdf",
                fileKey1,
                (long) dummyPdfContent.length,
                "application/pdf",
                FileFormat.PDF,
                14,
                studentCarlos,
                calculo1,
                topicIntegral
        );
        note1.setTags("cálculo, integrales, exámenes pasados, ejercicios");
        note1.setViewsCount(412);
        note1.setStatus(NoteStatus.PUBLISHED);
        noteRepository.save(note1);

        Rating r1 = new Rating(studentAna, note1, 5, "¡Excelente resumen! Me sirvió muchísimo para aprobar el parcial.");
        ratingRepository.save(r1);

        Note note2 = new Note(
                "Guía práctica: Dinámica rotacional y Leyes de Newton",
                "Formulario completo, diagramas de cuerpo libre y problemas resueltos de exámenes finales de Física I.",
                "guia_newton_fisica.pdf",
                fileKey2,
                (long) dummyPdfContent.length,
                "application/pdf",
                FileFormat.PDF,
                8,
                studentCarlos,
                fisica1,
                topicNewton
        );
        note2.setTags("física, newton, dinámica, vectores");
        note2.setViewsCount(280);
        note2.setStatus(NoteStatus.PUBLISHED);
        noteRepository.save(note2);

        Note note3 = new Note(
                "Resumen: Transformaciones lineales y subespacios",
                "Explicación intuitiva de cambio de base, núcleo, imagen y valores propios.",
                "transformaciones_lineales.pdf",
                fileKey3,
                (long) dummyPdfContent.length,
                "application/pdf",
                FileFormat.PDF,
                10,
                studentAna,
                algebra,
                topicEspacios
        );
        note3.setTags("álgebra lineal, transformaciones, subespacios");
        note3.setViewsCount(195);
        note3.setStatus(NoteStatus.PUBLISHED);
        noteRepository.save(note3);

        log.info("Seed de datos iniciales completado con éxito. Credenciales de acceso de prueba:");
        log.info("  Admin:      admin@uni.edu.pe / Password123!");
        log.info("  Moderador:  moderador@uni.edu.pe / Password123!");
        log.info("  Estudiante: ana@uni.edu.pe / Password123!");
    }

    private Subject createSubject(String code, String name, String desc, int credits, Set<AcademicProgram> programs) {
        Subject subject = new Subject();
        subject.setCode(code);
        subject.setName(name);
        subject.setDescription(desc);
        subject.setCredits(credits);
        subject.setActive(true);
        subject.setAcademicPrograms(programs);
        return subjectRepository.save(subject);
    }

    private void enrollStudentInSubject(Student student, Subject subject, String period) {
        Enrollment enrollment = new Enrollment(student, subject, period);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollmentRepository.save(enrollment);
    }

    private MultipartFile createDummyMultipartFile(String originalFilename, byte[] content) {
        return new MultipartFile() {
            @Override
            public String getName() {
                return "file";
            }

            @Override
            public String getOriginalFilename() {
                return originalFilename;
            }

            @Override
            public String getContentType() {
                return "application/pdf";
            }

            @Override
            public boolean isEmpty() {
                return content.length == 0;
            }

            @Override
            public long getSize() {
                return content.length;
            }

            @Override
            public byte[] getBytes() {
                return content;
            }

            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(content);
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {
                java.nio.file.Files.write(dest.toPath(), content);
            }
        };
    }
}
