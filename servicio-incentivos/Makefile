.PHONY: up down deploy import activate export-workflows save

# El dashboard/editor de n8n queda deshabilitado (N8N_DISABLE_UI=true en el
# docker-compose): estos comandos levantan directamente el workflow.

up:
	docker compose up -d

down:
	docker compose down

deploy: up
	@echo "Esperando que n8n levante..."
	@sleep 5
	@docker compose exec n8n n8n import:workflow --separate --input=/home/node/.n8n/workflows
	@$(MAKE) activate
	@echo "Workflow importado y activado (sin dashboard)."

import:
	docker compose exec n8n n8n import:workflow --separate --input=/home/node/.n8n/workflows

# El import deja el workflow inactivo; hay que publicarlo explícitamente y
# reiniciar para que la activación tome efecto (n8n no la aplica en caliente).
activate:
	@docker compose exec n8n sh -c "n8n publish:workflow --id=servicio-incentivos-difusion || n8n update:workflow --id=servicio-incentivos-difusion --active=true" || true
	@docker compose restart n8n

export-workflows:
	docker compose exec n8n n8n export:workflow --all --separate --output=/home/node/.n8n/workflows
	@for f in workflows/*.json; do \
		name=$$(python3 -c "import json,sys,re; d=json.load(open('$$f')); print(re.sub(r'[^a-zA-Z0-9_-]', '_', d['name'].lower().strip()))"); \
		mv "$$f" "workflows/$${name}.json"; \
	done
	@echo "Workflows exportados a ./workflows"

save: export-workflows
	git add workflows/
	git commit -m "chore: update n8n workflows"
