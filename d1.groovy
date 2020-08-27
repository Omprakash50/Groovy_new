job("task6_job1") {
  description("Download the data from github")
               scm {
                       github('Omprakash50/kubernetes')
                    }
               triggers {
                       scm(" * * * * * ")
                    }
               steps {
                       shell('''sudo cp * /root/web_fol''')
                    }
}
job("task6_job2") {
     description("checking the code and launching the pods")
    scm {
                       github('Omprakash50/kubernetes')
                    }
     triggers {
                 upstream('task6_job1' , 'SUCCESS')
        }
      steps {
             shell('''sudo cp * /root/web_fol
if sudo ls /root/web_fol | grep .php
then
if sudo kubectl get pods --kubeconfig /root/kubeconfig| grep php-container
then
echo "already running"
output=$(sudo kubectl get pods --kubeconfig /root/kubeconfig | grep php-container | cut -d ' ' -f 1)
sudo kubectl cp /root/web_fol/first.html $output:/var/www/html/ --kubeconfig /root/kubeconfig
else
sudo kubectl apply -f /root/web_fol/php.yml --kubeconfig /root/kubeconfig
sleep 30
output=$(sudo kubectl get pods --kubeconfig /root/kubeconfig | grep php-container | cut -d ' ' -f 1)
sudo kubectl cp /root/web_fol/first.html $output:/var/www/html/ --kubeconfig /root/kubeconfig
fi
elif sudo ls /root/web_fol | grep .html
then
if sudo kubectl get pods --kubeconfig /root/kubeconfig | grep html-container
then
echo "already running"
output=$(sudo kubectl get pods --kubeconfig /root/kubeconfig | grep html-container | cut -d ' ' -f 1)
sudo kubectl cp /root/web_fol/first.html $output:/var/www/html/ --kubeconfig /root/kubeconfig
else
sudo kubectl apply -f /root/web_fol/html.yml --kubeconfig /root/kubeconfig
sleep 30
output=$(sudo kubectl get pods --kubeconfig /root/kubeconfig | grep html-container | cut -d ' ' -f 1)
sudo kubectl cp /root/web_fol/first.html $output:/var/www/html/ --kubeconfig /root/kubeconfig
fi
else
sudo echo "Don't have environment for this file"
fi''')
         }
}
job("task6_job3") {
      description("Testing the code by seeing its status code")
      triggers {
                upstream('task6_job2' , 'SUCCESS')
         }
      steps {
             shell('''if sudo ls /root/web_fol | grep .php
then
status=$(curl -s -i -w "%{http_code}" -o /dev/null 192.168.99.107:30000/first.php)
elif sudo ls /root/web_fol | grep .html
then
status=$(curl -s -i -w "%{http_code}" -o /dev/null 192.168.99.107:30000/first.html)
else
sudo echo "Don't have environment for this file"
fi

if [[ $status == 200 ]] ; then exit 0  ; else sudo curl --user 'admin:Omkar@jio.com1' http://192.168.99.109:8080/job/Notify/build?token=mail ;fi''')
      }    
}
job("task6_job4") {
      description("Sending Email if error occur in the code otherwise not")
     
       triggers {
        scm('mail')
    }
     triggers {
                  upstream('task6_job3' , 'SUCCESS')
            }
      steps {
                  shell('''sudo python3 /root/mail.py''')
            }
}            
