.PHONY: up down deploy import-workflows

up:
	docker compose up -d

down:
	docker compose down

deploy: up
	@echo "Esperando que n8n levante..."
	@sleep 5
	@docker compose exec n8n n8n import:workflow --separate --input=/home/node/.n8n/workflows
	@echo "Workflows importados."

import:
	docker compose exec n8n n8n import:workflow --separate --input=/home/node/.n8n/workflows

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
