
depends: ## Install & build dependencies
	go get ./...
	go build ./...
	go mod tidy

lint: ## Run Go linters
	golangci-lint run

build.linux: clean ## Build the server binary file for Linux host
	GOOS=linux GOARCH=amd64 sh scripts/build.sh

clean: ## Clean up the built & test files
	rm -rf ./server ./*.out
	rm -rf .serverless
	
specs: ## Generate swagger specs
	HOST=$(HOST) sh scripts/specs-gen.sh

dev.migrate: dev.deployfunc ## Migrate on DEV environment
	sh scripts/sls-funcs.sh dev invoke --function Migration

dev.deploy: ## Deploy to DEV environment
	sh scripts/sls.sh dev deploy

%: # prevent error for `up` target when passing arguments
ifeq ($(filter up,$(MAKECMDGOALS)),up)
	@:
else
	$(error No rule to make target `$@`.)
endif

