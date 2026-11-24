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
        if (repository.count() != 0) {
            corrigirExemplosComCodificacaoInvalida();
            return;
        }
        service.abrirChamado(new AbrirChamadoRequest("Impressora sem conexão", "Exemplo fictício: impressora do setor financeiro não conecta à rede."), "demo");
        service.abrirChamado(new AbrirChamadoRequest("Acesso ao sistema corporativo", "Exemplo fictício: usuário solicita verificação de acesso ao sistema."), "demo");
        Chamado resolvido = service.abrirChamado(new AbrirChamadoRequest("Atualização de estação de trabalho", "Exemplo fictício: atualizar os aplicativos da estação de atendimento."), "demo");
        service.resolverChamado(resolvido.getId(), new ResolucaoChamadoRequest("Aplicativos atualizados e funcionamento validado."), "demo");
    }

    // Repara somente os textos dos exemplos fictícios da primeira publicação.
    // Chamados de visitantes, status, soluções e datas são preservados.
    private void corrigirExemplosComCodificacaoInvalida() {
        for (Chamado chamado : repository.findByUsuario("demo")) {
            String titulo = chamado.getTitulo();
            String descricao = chamado.getDescricao();
            if (titulo == null || (!titulo.contains("\uFFFD")
                    && (descricao == null || !descricao.contains("\uFFFD")))) continue;
            if (titulo.startsWith("Impressora sem")) {
                chamado.setTitulo("Impressora sem conexão");
                chamado.setDescricao("Exemplo fictício: impressora do setor financeiro não conecta à rede.");
            } else if (titulo.startsWith("Acesso ao sistema")) {
                chamado.setTitulo("Acesso ao sistema corporativo");
                chamado.setDescricao("Exemplo fictício: usuário solicita verificação de acesso ao sistema.");
            } else if (titulo.startsWith("Atualiza")) {
                chamado.setTitulo("Atualização de estação de trabalho");
                chamado.setDescricao("Exemplo fictício: atualizar os aplicativos da estação de atendimento.");
            } else {
                continue;
            }
            repository.save(chamado);
        }
    }
}
