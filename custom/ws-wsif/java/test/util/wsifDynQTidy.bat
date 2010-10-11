rem -----------------------------------------------------------------
rem deletes the MQ dynamic queues created while running the testcases
rem -----------------------------------------------------------------

echo DIS QUEUE(AMQ*) > disqs.mqs

runmqsc < disqs.mqs > qs.mqs

call java util.GetQueues qs.mqs dels.mqs

runmqsc < dels.mqs

del disqs.mqs
del qs.mqs
del dels.mqs
