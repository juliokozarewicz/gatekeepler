FROM nginx:latest

# Instala envsubst
RUN apt-get update && apt-get install -y gettext-base

# Copia o template do nginx
COPY nginx.conf.template /etc/nginx/nginx.conf.template

# Cria diretório de cache, logs etc se necessário
RUN mkdir -p /var/log/nginx /tmp/nginx_cache

# Comando de entrada: substitui variáveis e inicia nginx
CMD envsubst '$PRIVATE_DOMAIN $HELLOWORLD_PORT $DOCUMENTATION_PORT $ACCOUNTS_PORT' \
    < /etc/nginx/nginx.conf.template > /etc/nginx/nginx.conf && \
    nginx -g 'daemon off;'
