async function enviar(url, method, dados, form) {
  const button = form.querySelector('button');
  const message = form.querySelector('[role="alert"]');
  button.disabled = true;
  message.hidden = true;
  try {
    const response = await fetch(url, { method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(dados) });
    if (response.status === 401) { window.location.assign('/login'); return; }
    if (!response.ok) {
      const messages = {400:'Preencha os campos obrigat�rios.',404:'Chamado n�o encontrado. Atualize a p�gina.',409:'Este chamado j� foi resolvido. Atualize a p�gina.'};
      throw new Error(messages[response.status] || 'N�o foi poss�vel salvar. Tente novamente.');
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
