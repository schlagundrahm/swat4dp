ANT-Reorg MacroDefs/Targets
---------------------------

2013-04-05 Current State
------------------------
				
swat-dp-tools
-------------------------------------------------------------

chooser.xml:
	info*
	load-infra-props
	M load-infra-props-macro
	choose-zone
	load-zone-props
	M load-zone-props-macro
	choose-env
	load-env-props
	M load-env-props-macro
	choose-domain
	load-domain-props
	M load-domain-props-macro
	choose-service
	load-service-props
	M load-service-props-macro
	choose-deployment-policy
	choose-device-set
	load-device-set-props
	M load-device-set-props-macro
	choose-device
	load-device-props
	M load-device-props-macro
	choose-multi-device
	load-user
	M load-user-macro
	choose-password
	load-password
	M load-password-macro
	choose-crypto-prefix
	cancel
	
crypto.xml:
	info*
	key-generate-on-device
	M key-generate-on-device-macro
	upload-signer-cert
	M upload-signer-cert-macro
	import-crypto-object
	M import-crypto-object-macro
	M export-crypto-object-macro

crypto-hsm.xml:
	info*
	TODO M exchange-hsm-kwk-on-device-set-macro
	TODO M exchange-hsm-kwk-macro

device.xml:
	info*
	M device-export-macro
	M device-export-objects-macro
	M device-deploy-macro
	
device-crypto.xml:
	info*
	M device-key-generate-macro

device-pack.xml:
	info*
	M device-prepare-import-dirs-macro
	M device-pack-macro
	M device-pack2compare-macro

device-split.xml:
	info*
	M device-split-objects-macro

device-targets.xml:
	info*
	init
	10-device-deploy
	11-device-export
	12-device-pack2compare
	15-device-key-generate
	18-device-xcfg-reformat
	19-device-project-clean
	50-ops-ping-host
	
dialogs.xml:
	info*
	M dialog-confirm-productive-macro
	M dialog-confirm-macro
	M dialog-password-macro
	M dialog-export-macro
	M dialog-deployment-options-macro
		
domain.xml:
	info*
	24-domain-cleanup-on-device
	domain-pack-service-xcfg
	M domain-pack-service-xcfg-macro
	M domain-export-objects-macro
	
mail-macros.xml:
	info*
	M send-mail-macro
	M check-mail-address-macro

ops-macros.xml:
	info*
	M ping-host-macro

pw.xml:
	info*
	M init-env-passwords-macro
	M init-device-passwords-macro
	prompt-for-variable-env-pw
	prompt-for-variable-device-pw
	M prompt-for-variable-passwd-macro
	encrypt-password
	decrypt-password
	
service.xml:
	info*
	M service-export-macro
	M service-export-objects-macro
	M service-prepare-export-objects-macro
	M service-deploy-macro
	service-deploy-on-secondary-devices
	M service-create-project-prefix-macro
		
service-crypto.xml:
	info*
	M service-key-generate-macro
	M service-cert-distribute-macro

service-pack.xml:
	info*
	M service-prepare-import-dirs-macro
	M service-pack-macro
	M service-pack2compare-macro
	
service-split.xml:
	info*
	M service-split-objects-macro
	
service-targets.xml:
	info*
	init
	30-service-deploy
	31-service-deploy-with-options
	32-service-export
	33-service-pack2compare
	34-service-object-list-create
	35-service-key-generate
	36-service-key-distribute
	37-service-cert-distribute
	38-service-xcfg-reformat
	39-service-project-clean
	
soma.xml:
	info*
	M soma-prepare-request-macro
	M soma-run-multi-requests-on-multi-devices-macro
	soma-run-multi-requests-on-single-device
	M soma-run-multi-requests-on-single-device-macro
	soma-run-single-request-on-single-device
	M soma-run-single-request-on-single-device-macro

soma-filter.xml:
	info*
	M soma-define-content-filter-macro
	M soma-define-file-filter-macro
	M soma-define-device-filter-macro
	M soma-define-env-filter-macro
	M soma-define-crypto-filter-macro
	M soma-define-custom-filter-macro
	M soma-define-filter-macro
		
taskdefs.xml:
	info*
	declare

template-targets.xml:
	info*
	init
	seed-service
	clone-project
	M seed-svc-template-macro

tokenize.xml:
	info*
	M tokenize-configuration-macro

xform-macros.xml:
	info*
	M extract-objects-macro
	M extract-object-list-macro	
	M extract-soma-status-macro
	M filter-with-object-list-macro
	M create-service-export-request-macro
	M create-delete-object-list-request-macro
	M combine-prefix-xcfg-files-macro
	M combine-xcfg-files-macro
	M verify-filter-xcfg-files-macro
	M verify-soma-response-macro
	M extract-xpath-macro
	M extract-zip-file-macro
	M extract-file-macro
	M delete-xcfg-if-empty-macro
	M copy-files-content-macro
	M copy-xcfg-content-macro
	M copy-soma-request-macro
	M xcfg-reformat-macro
	M join-vars-macro
	M base64-encode-macro
	M base64-decode-macro
	M decrypt-password-macro
	
top: macros.xml:
	info*
		
Other projects
-------------------------------------------------------------

<cust>-swat-dp-devices/build.xml:
	all imported from device-targets.xml
	
<cust>-swat-dp-services-<zone>-<domain>-*/build.xml:
	all imported from service-targets.xml

swat-dp-service-templates/build.xml:
	all imported from template-targets.xml

Swat4DP project
-------------------------------------------------------------
	
Swat4DP/build.xml:
	info* (not visible)
	10-device-deploy
	11-device-export
	12-device-pack2compare
	15-device-key-generate
	18-device-xcfg-reformat
	19-device-project-clean
	20-domain-services-deploy
	21-domain-services-export
	22-domain-services-pack2compare
	23-domain-services-object-list-create
	24-domain-cleanup
	30-service-deploy
	31-service-deploy-with-options
	32-service-export
	33-service-pack2compare
	34-service-object-list-create
	35-service-key-generate
	36-service-key-distribute
	37-service-cert-distribute
	38-service-xcfg-reformat
	39-service-project-clean
	40-service-template-seed
	41-service-template-clone
	50-ops-ping-host
	51-ops-sanitize-error-report
	90-swat-clean-all-projects
	91-swat-show-devices
	92-swat-show-device-sets
	93-swat-show-user-pw-properties
	94-swat-encrypt-user-settings
	95-swat-decrypt-user-settings
	sanitizeTextFile (private, used by 51-ops-sanitize-error-report)

Swat4DP/build-hsm.xml:
	info* (not visible)
	91-exchange-hsm-kwk-on-device-set
	92-exchange-hsm-kwk-between-two-devices

Call Hierarchy Top-Down
-------------------------------------------------------------
	
Swat4DP Call Hierarchy:
	info* (not visible)
		info(macros)
	10-device-deploy
		declare,choose-device,10-device-deploy(device-targets)
	11-device-export
		declare,choose-device,11-device-export(device-targets)
	12-device-pack2compare
		declare,choose-device,12-device-pack2compare(device-targets)
	15-device-key-generate
		declare,choose-device,15-device-key-generate(device-targets)
	18-device-xcfg-reformat
		declare,choose-device,18-device-xcfg-reformat(device-targets)
	19-device-project-clean
		declare,99-project-clean(device-targets)
	20-domain-services-deploy
		declare,load-domain-props,FE(30-service-deploy)
	21-domain-services-export
		declare,load-domain-props,FE(31-service-export)
	22-domain-services-pack2compare
		declare,load-domain-props,FE(32-service-pack2compare)
	23-domain-services-object-list-create
		declare,load-domain-props,FE(33-service-object-list-create)
	24-domain-cleanup
		declare,load-domain-props,load-device-set-props-macro,
		FE(24-domain-cleanup-on-device(domain))
	30-service-deploy
		declare,load-service-props,30-service-deploy(service-targets)
	31-service-deploy-with-options
		declare,load-service-props,31-service-deploy-with-options(service-targets)
	32-service-export
		declare,load-service-props,32-service-export(service-targets)
	33-service-pack2compare
		declare,load-service-props,33-service-pack2compare(service-targets)
	34-service-object-list-create
		declare,load-service-props,34-service-object-list-create(service-targets)
	35-service-key-generate
		declare,load-service-props,35-service-key-generate(service-targets)
	36-service-key-distribute
		declare,load-service-props,36-service-key-distribute(service-targets)
	37-service-cert-distribute
		declare,load-service-props,37-service-cert-distribute(service-targets)
	38-service-xcfg-reformat
		declare,load-service-props,38-service-xcfg-reformat(service-targets)
	39-service-project-clean
		declare,load-service-props,39-service-project-clean(service-targets)
	40-service-template-seed
		declare,seed-service(template-targets)
	41-service-template-clone
		declare,clone-project(template-targets)
	50-ops-ping-host
		declare,choose-device,50-ops-ping-host(device-targets)
	99-swat-clean-all-projects
		declare
	sanitizeErrorReport
		declare,FE(sanitizeTextFile)
	sanitizeTextFile
		-

Device-Targets Call Hierarchy:
	10-device-deploy
		declare,init,load-device-props,choose-deployment-policy,
		device-pack-macro,device-deploy-macro
	11-device-export
		declare,init,choose-device,device-export-macro
	12-device-pack2compare
		declare,init,choose-device,device-pack2compare-macro
	15-device-key-generate
		declare,init,choose-device-set,device-key-generate-macro
	18-device-xcfg-reformat
		declare,xcfg-reformat-macro			
	19-device-project-clean
		-			
	50-ops-ping-host
		declare,init,choose-device,ping-host-macro
		
Domain Call Hierarchy:
	24-domain-cleanup-on-device
		load-device-props-macro,domain-export-objects-macro,FE(domain-pack-service-xcfg),combine-xcfg-files-macro,
		extract-object-list-macro,filter-with-object-list-macro,
		create-delete-object-list-request-macro,extract-xpath-macro,dialog-confirm-macro,
		soma-run-single-request-on-single-device-macro
		
Service-Targets Call Hierarchy:
	30-service-deploy
		declare,init,load-service-props,choose-deployment-policy,dialog-confirm-productive-macro,service-pack-macro,service-deploy-macro
	31-service-deploy-with-options
		declare,init,load-service-props,choose-deployment-policy,dialog-confirm-productive-macro,service-pack-macro,dialog-deployment-options-macro,service-deploy-macro
	32-service-export
		declare,init,load-service-props,load-device-set-props-macro,dialog-export-macro,service-export-macro,tokenize-configuration-macro
	33-service-pack2compare
		declare,init,choose-env,service-pack2compare-macro
	34-service-object-list-create
		declare,init,choose-env,service-pack2compare-macro,combine-xcfg-files-macro,extract-object-list-macro
	35-service-key-generate
		declare,init,choose-env,choose-crypto-prefix,dialog-confirm-productive-macro,service-key-generate-macro
	36-service-key-distribute
		declare,init,choose-env,choose-crypto-prefix,dialog-confirm-productive-macro,service-key-generate-macro
	37-service-cert-distribute
		declare,init,choose-env,choose-crypto-prefix,dialog-confirm-productive-macro,service-cert-distribute-macro
	38-service-xcfg-reformat
		declare,xcfg-reformat-macro
	39-service-project-clean
		-

Template-Targets Call Hierarchy:
	seed-service
		init,choose-domain,service-create-project-prefix-macro,seed-svc-template-macro
	clone-project
		init,choose-domain,service-create-project-prefix-macro
