#!/bin/bash

# Script para gestionar el entorno Docker de StockChef
# Uso: ./docker-manager.sh [comando] [opciones]

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Función para mostrar ayuda
show_help() {
    echo -e "${BLUE}StockChef Docker Manager${NC}"
    echo ""
    echo "Uso: $0 [comando] [opciones]"
    echo ""
    echo "Comandos disponibles:"
    echo "  up [mysql|postgres]    - Iniciar el stack con la base de datos especificada"
    echo "  down                   - Parar y remover todos los contenedores"
    echo "  restart [mysql|postgres] - Reiniciar el stack"
    echo "  logs [servicio]        - Mostrar logs de un servicio específico"
    echo "  status                 - Mostrar estado de los servicios"
    echo "  clean                  - Limpiar volúmenes y contenedores"
    echo "  tools                  - Iniciar herramientas de administración"
    echo "  build                  - Reconstruir la imagen de la aplicación"
    echo "  help                   - Mostrar esta ayuda"
    echo ""
    echo "Ejemplos:"
    echo "  $0 up mysql           # Iniciar con MySQL"
    echo "  $0 up postgres        # Iniciar con PostgreSQL"
    echo "  $0 logs backend       # Ver logs del backend"
    echo "  $0 tools              # Iniciar phpMyAdmin y pgAdmin"
}

# Función para configurar el entorno
setup_env() {
    local db_type=${1:-mysql}
    
    if [ ! -f .env ]; then
        echo -e "${YELLOW}Creando archivo .env desde .env.example${NC}"
        cp .env.example .env
    fi
    
    # Actualizar tipo de base de datos en .env
    if command -v sed >/dev/null 2>&1; then
        sed -i "s/DATABASE_TYPE=.*/DATABASE_TYPE=$db_type/" .env
    else
        echo -e "${YELLOW}Por favor, actualiza DATABASE_TYPE=$db_type en el archivo .env${NC}"
    fi
    
    echo -e "${GREEN}Configurado para usar: $db_type${NC}"
}

# Función para iniciar servicios
start_services() {
    local db_type=${1:-mysql}
    setup_env $db_type
    
    echo -e "${BLUE}Iniciando StockChef con $db_type...${NC}"
    
    case $db_type in
        mysql)
            docker-compose up -d mysql stockchef-backend
            ;;
        postgres)
            docker-compose up -d postgres stockchef-backend
            ;;
        *)
            echo -e "${RED}Tipo de base de datos no válido: $db_type${NC}"
            echo "Usa: mysql o postgres"
            exit 1
            ;;
    esac
    
    echo -e "${GREEN}Servicios iniciados exitosamente${NC}"
    echo -e "${YELLOW}Backend disponible en: http://localhost:8090/api/health${NC}"
}

# Función para parar servicios
stop_services() {
    echo -e "${YELLOW}Parando todos los servicios...${NC}"
    docker-compose down
    echo -e "${GREEN}Servicios parados${NC}"
}

# Función para reiniciar servicios
restart_services() {
    local db_type=${1:-mysql}
    echo -e "${YELLOW}Reiniciando servicios...${NC}"
    stop_services
    start_services $db_type
}

# Función para mostrar logs
show_logs() {
    local service=${1:-stockchef-backend}
    
    case $service in
        backend|app|stockchef-backend)
            docker-compose logs -f stockchef-backend
            ;;
        mysql|db-mysql)
            docker-compose logs -f mysql
            ;;
        postgres|db-postgres)
            docker-compose logs -f postgres
            ;;
        all)
            docker-compose logs -f
            ;;
        *)
            docker-compose logs -f $service
            ;;
    esac
}

# Función para mostrar estado
show_status() {
    echo -e "${BLUE}Estado de los servicios:${NC}"
    docker-compose ps
    echo ""
    echo -e "${BLUE}Uso de recursos:${NC}"
    docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}" 2>/dev/null || echo "No hay contenedores ejecutándose"
}

# Función para limpiar
clean_all() {
    echo -e "${YELLOW}¿Estás seguro de que quieres limpiar todos los datos? (y/N)${NC}"
    read -r response
    if [[ "$response" =~ ^[Yy]$ ]]; then
        echo -e "${YELLOW}Limpiando contenedores y volúmenes...${NC}"
        docker-compose down -v --remove-orphans
        docker system prune -f
        echo -e "${GREEN}Limpieza completada${NC}"
    else
        echo -e "${BLUE}Operación cancelada${NC}"
    fi
}

# Función para iniciar herramientas de administración
start_tools() {
    echo -e "${BLUE}Iniciando herramientas de administración...${NC}"
    docker-compose --profile tools up -d phpmyadmin pgadmin
    echo -e "${GREEN}Herramientas iniciadas:${NC}"
    echo -e "${YELLOW}phpMyAdmin: http://localhost:8080${NC}"
    echo -e "${YELLOW}pgAdmin: http://localhost:8081${NC}"
}

# Función para reconstruir la aplicación
rebuild_app() {
    echo -e "${BLUE}Reconstruyendo la imagen de la aplicación...${NC}"
    docker-compose build --no-cache stockchef-backend
    echo -e "${GREEN}Imagen reconstruida exitosamente${NC}"
}

# Main
case ${1:-help} in
    up|start)
        start_services ${2:-mysql}
        ;;
    down|stop)
        stop_services
        ;;
    restart)
        restart_services ${2:-mysql}
        ;;
    logs)
        show_logs ${2:-backend}
        ;;
    status)
        show_status
        ;;
    clean)
        clean_all
        ;;
    tools)
        start_tools
        ;;
    build)
        rebuild_app
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        echo -e "${RED}Comando no reconocido: $1${NC}"
        echo ""
        show_help
        exit 1
        ;;
esac