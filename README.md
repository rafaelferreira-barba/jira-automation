# Jira Automation

Ferramenta para criação automatizada de **User Stories** e **Sub-tasks** no Jira a partir de arquivos CSV.

---

## 1. Configuração do Jira

### 1.1 Gerar token de API

1. Acesse [https://id.atlassian.com/manage-profile/security/api-tokens](https://id.atlassian.com/manage-profile/security/api-tokens)
2. Clique em **Create API token**.
3. Dê um nome para o token (ex.: "Jira Automation Tool") e clique em **Create**.
4. Copie o token gerado — você vai precisar dele no próximo passo.

---

### 1.2 Criar o arquivo `jira.properties`

Na raiz do projeto (mesmo nível do `.exe` gerado), crie um arquivo chamado `jira.properties` com o seguinte conteúdo:

```properties
# Email usado no Jira
jira.email=seu-email@exemplo.com

# Token de API gerado no passo anterior
jira.token=seu-token-aqui

# Domínio do Jira (sem https://)
jira.domain=seu-domínio.atlassian.net

# Key do projeto no Jira
jira.project=PROJ
