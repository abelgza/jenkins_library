#!/usr/bin/env groovy

def call(String branch = "dev", String port= "Undefined", String project_name= "Undefined", String git_test_project="dev") {
	try {
		stage ("Inicio ${branch}") {
			echo "Se inicia proceso en ${branch}, puerto ${port}"
		}
		stage ("Se instalan dependencias") {
			bat label: "npm install", script: "npm install"
		}
		stage ("Deploy") {
			bat label: "Se inicializa con pm2", script: "C:\\Users\\aagonzalez\\AppData\\Roaming\\npm\\pm2 start server/server.js --name ${branch}_${project_name}"
		}
		dir("pruebas") {
			stage("Descarga de pruebas") {
				echo "se descarga el código de pruebas"
				git branch: "${branch}", url: "${git_test_project}"
			}
			stage("Pruebas Robot") {
				bat label:" Se ejecuta la prueba con robot", script:"robot *.robot"
			}
			stage("Pruebas Newman") {
				bat label:"Se ejecuta prueba con newman", script:"C:\\Users\\aagonzalez\\AppData\\Roaming\\npm\\newman run newman_* -r html"
			}
		}

		} catch (Exception err) {
			echo "Hubo errores, se realiza Rollback"
			bat label:" Rollback y se elimina de pm2", script:"pm2 delete ${branch}_${project_name}"
			currentBuild.result = "FAILURE"
		} finally{
			bat label: "Se genera el ZIP de la evidencia", script: "jar -cMf ${project_name}_evidencia.zip pruebas"
		}
	}
