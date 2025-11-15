package com.tourney.user_service.controller;

import com.tourney.user_service.domain.TestEntity;
import com.tourney.user_service.repository.TestRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@Tag(name = "Test", description = "API testowe do sprawdzenia działania aplikacji")
public class TestController {

    private final TestRepository testRepository;

    @Operation(summary = "Utwórz nowy test", description = "Tworzy nowy obiekt testowy w bazie danych")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Test utworzony pomyślnie",
                    content = @Content(schema = @Schema(implementation = TestEntity.class))),
        @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe"),
        @ApiResponse(responseCode = "500", description = "Błąd serwera")
    })
    @PostMapping
    public ResponseEntity<TestEntity> createTest(
            @Parameter(description = "Obiekt testowy do utworzenia") @RequestBody(required = false) TestEntity test) {
        TestEntity newTest = test != null ? test : new TestEntity();
        return ResponseEntity.ok(testRepository.save(newTest));
    }

    @Operation(summary = "Pobierz wszystkie testy", description = "Zwraca listę wszystkich obiektów testowych")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista testów pobrana pomyślnie"),
        @ApiResponse(responseCode = "500", description = "Błąd serwera")
    })
    @GetMapping
    public ResponseEntity<Iterable<TestEntity>> getAllTests() {
        return ResponseEntity.ok(testRepository.findAll());
    }
}