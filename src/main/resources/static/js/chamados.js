async function enviar(url, method, dados, form) {
  const button = form.querySelector('button');
  const message = form.querySelector('[role="alert"]');
  button.disabled = true;
  message.hidden = true;
  try {
    const response = await fetch(url, { method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(dados) });
    if (response.status === 401) { window.location.assign('/login'); return; }
    if (!response.ok) {
      const messages = {400:'Preencha os campos obrigatórios.',404:'Chamado não encontrado. Atualize a página.',409:'Este chamado já foi resolvido. Atualize a página.'};
      throw new Error(messages[response.status] || 'Não foi possível salvar. Tente novamente.');
    }
    window.location.assign('/chamados');
  } catch(error) { message.textContent = error.message; message.hidden = false; button.disabled = false; }
}
document.querySelector('#novo-chamado')?.addEventListener('submit', event => {
  event.preventDefault();
  const form = event.currentTarget;
  enviar('/api/chamados', 'POST', { titulo:form.elements.titulo.value.trim(), descricao:form.elements.descricao.value.trim() }, form);
});
document.querySelectorAll('.resolver-chamado').forEach(form => form.addEventListener('submit', event => {
  event.preventDefault();
  enviar('/api/chamados/' + encodeURIComponent(form.dataset.id) + '/resolver', 'PUT', {solucao:form.elements.solucao.value.trim()}, form);
}));
