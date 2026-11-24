package com.pfc;

import com.pfc.thindesk.dto.AbrirChamadoRequest;
import com.pfc.thindesk.dto.ResolucaoChamadoRequest;
import com.pfc.thindesk.entity.Chamado;
import com.pfc.thindesk.repository.ChamadoRepository;
import com.pfc.thindesk.service.ChamadoService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@Order(10)
public class DemoDataConfig implements CommandLineRunner {
    private final ChamadoRepository repository;
    private final ChamadoService service;

    public DemoDataConfig(ChamadoRepository repository, ChamadoService service) {
        this.repository = repository;
        this.service = service;
    }

    @Override
    public void run(String... args) {
        if (repository.count() != 0) return;
        service.abrirChamado(new AbrirChamadoRequest("Impressora sem conex�o", "Exemplo fict�cio: impressora do setor financeiro n�o conecta � rede."), "demo");
        service.abrirChamado(new AbrirChamadoRequest("Acesso ao sistema corporativo", "Exemplo fict�cio: usu�rio solicita verifica��o de acesso ao sistema."), "demo");
        Chamado resolvido = service.abrirChamado(new AbrirChamadoRequest("Atualiza��o de esta��o de trabalho", "Exemplo fict�cio: atualizar os aplicativos da esta��o de atendimento."), "demo");
        service.resolverChamado(resolvido.getId(), new ResolucaoChamadoRequest("Aplicativos atualizados e funcionamento validado."), "demo");
    }
}
