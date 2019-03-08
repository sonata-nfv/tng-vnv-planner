#!/usr/bin/env groovy
/*
 * Copyright (c) 2015 SONATA-NFV, 2017 5GTANGO [, ANY ADDITIONAL AFFILIATION]
 * ALL RIGHTS RESERVED.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Neither the name of the SONATA-NFV, 5GTANGO [, ANY ADDITIONAL AFFILIATION]
 * nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * This work has been performed in the framework of the SONATA project,
 * funded by the European Commission under Grant number 671517 through
 * the Horizon 2020 and 5G-PPP programmes. The authors would like to
 * acknowledge the contributions of their colleagues of the SONATA
 * partner consortium (www.sonata-nfv.eu).
 *
 * This work has been performed in the framework of the 5GTANGO project,
 * funded by the European Commission under Grant number 761493 through
 * the Horizon 2020 and 5G-PPP programmes. The authors would like to
 * acknowledge the contributions of their colleagues of the 5GTANGO
 * partner consortium (www.5gtango.eu).
 */

pipeline {
    agent any
    stages {
      stage('test') {
         steps {
            timestamps {
                sh './gradlew clean test'
            }
         }
      }
      stage('build') {
         steps {
            timestamps {
                sh './gradlew dockerBuild -x test'
            }
         }
      }
        stage('push') {
            steps {
                timestamps {
                    sh 'docker push registry.sonata-nfv.eu:5000/tng-vnv-planner:latest'
                }
            }
        }
        stage('Deployment in Pre Integration') {
          parallel {
            stage('Deployment in Pre Integration') {
              steps {
                echo 'Deploying in Pre integration...'
              }
            }
            stage('Deploying') {
              steps {
                sh 'rm -rf tng-devops || true'
                sh 'git clone https://github.com/sonata-nfv/tng-devops.git'
                dir(path: 'tng-devops') {
                  sh 'ansible-playbook roles/vnv.yml -i environments -e "target=pre-int-vnv-bcn.5gtango.eu" -e "component=planner"'
                }
              }
            }
          }
        }
        stage('Promoting containers to integration env') {
          when {
             branch 'master'
          }
          parallel {
            stage('Publishing containers to int') {
              steps {
                echo 'Promoting containers to integration'
              }
            }
            stage('tng-vnv-planner') {
              steps {
                sh 'docker tag registry.sonata-nfv.eu:5000/tng-vnv-planner:latest registry.sonata-nfv.eu:5000/tng-vnv-planner:int'
                sh 'docker push  registry.sonata-nfv.eu:5000/tng-vnv-planner:int'
              }
            }
          }
        }
    }
    post {
        always {
            junit(allowEmptyResults: true, testResults: 'build/test-results/**/*.xml')
            publishHTML (target: [
                  allowMissing: true,
                  alwaysLinkToLastBuild: false,
                  reportDir: 'build/reports/tests/test',
                  reportFiles: 'index.html',
                  reportName: "Test Report"
            ])
        }
        success {
            emailext (
              subject: "SUCCESS: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
              body: """<p>SUCCESS: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
                <p>Check console output at &QUOT;<a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>&QUOT;</p>""",
            recipientProviders: [[$class: 'DevelopersRecipientProvider']]
            )
          }
        failure {
          emailext (
              subject: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
              body: """<p>FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
                <p>Check console output at &QUOT;<a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>&QUOT;</p>""",
              recipientProviders: [[$class: 'DevelopersRecipientProvider']]
            )
        }
    }
}
